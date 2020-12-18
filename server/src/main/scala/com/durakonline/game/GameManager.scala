package com.durakonline.game

import com.durakonline.model._
import com.durakonline.game.network.Messages.Request._
import com.durakonline.game.network.Messages.Response._

import org.http4s.Response

import io.circe.parser.decode, io.circe.syntax._

import cats.effect._
import cats.syntax.all._
import cats.effect.concurrent.Ref

import fs2.{ Stream, Pipe }
import fs2.concurrent.Queue

import org.http4s.server.websocket._
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame._

import scala.concurrent.duration._


case class GameManager [F[_] : Concurrent] (
  gameState: Option[GameState],
  players: Vector[Player],
  playersReady: Vector[Player]
) {
  def addPlayer (player: Player): GameManager[F] =
    copy(players = players :+ player)

  def addToReady (playerId: UUIDString): Either[String, GameManager[F]] =
    for {
      player <- players.find(_.id == playerId).toRight("no player with specified id")
    } yield copy(playersReady = playersReady :+ player)
}

object GameManager {
  def empty [F[_] : Concurrent]: F[Ref[F, GameManager[F]]] =
    Ref.of[F, GameManager[F]](GameManager[F](None, Vector.empty, Vector.empty))

  def markReady [F[_]] (manager: Ref[F, GameManager[F]], id: UUIDString): F[Text] =
    manager.modify(m => 
      m.addToReady(id) match {
        case Left(error) => (m, Text(Error(error).asJson.noSpaces))
        case Right(newManager) => (newManager, Text(OK.apply.asJson.noSpaces))
      }
    )

  def createConnection [F[_] : Concurrent : Timer] (
    manager: Ref[F, GameManager[F]], 
    player: Player
  )(implicit CF: ConcurrentEffect[F]): F[Response[F]] = {

    val echoReply: Pipe[F, WebSocketFrame, WebSocketFrame] =
      _.collect {
        case t @ Text(msg, _) => t
        case _ => Text("unsupported")
      }.evalMap{
        case Text(msg, _) => decode[Action](msg) match {
          case Left(error) => CF.delay(Text(Error(error.getMessage()).asJson.noSpaces))
          case Right(action) => action match {
            case MarkReady(_, playerId) => markReady(manager, playerId)
          }
        }
      }

    val playerNotifier = 
      Stream.repeatEval(manager.get.map(m => Text(m.toString))).metered(1.second)

    Queue
      .unbounded[F, WebSocketFrame]
      .flatMap { q =>
        WebSocketBuilder[F].build(
          receive = q.enqueue,
          send = q.dequeue.through(echoReply) merge playerNotifier
        )
      }
  }
}
