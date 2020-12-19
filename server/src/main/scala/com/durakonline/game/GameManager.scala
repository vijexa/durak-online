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
  lobby: Ref[F, Lobby[F]],
  gameState: Option[GameState],
  players: Vector[Player],
  playersReady: Vector[Player]
) {
  def getRoom [F[_]] (ref: Ref[F, GameManager[F]]) = 
    lobby.get.map(_.rooms.collectFirst{ 
      case (_, room) if room.gameManager == ref => room 
    })

  def addPlayer (player: Player): GameManager[F] =
    copy(players = players :+ player)

  def addToReady (playerId: UUIDString): Either[String, GameManager[F]] =
    for {
      player <- players.find(_.id == playerId).toRight("no player with specified id")
    } yield copy(playersReady = playersReady :+ player)
}

object GameManager {
  def empty [F[_] : Concurrent] (lobby: Ref[F, Lobby[F]]): F[Ref[F, GameManager[F]]] =
    Ref.of[F, GameManager[F]](GameManager[F](lobby, None, Vector.empty, Vector.empty))

  def markReady [F[_]] (manager: Ref[F, GameManager[F]], id: UUIDString): F[Text] =
    manager.modify(m => 
      m.addToReady(id) match {
        case Left(error) => (m, Text(Error(error).asJson.noSpaces))
        case Right(newManager) => (newManager, Text(OK.apply.asJson.noSpaces))
      }
    )

  def startGame [F[_] : Concurrent] (
    manager: Ref[F, GameManager[F]], 
    id: UUIDString
  ) (implicit CF: ConcurrentEffect[F]): F[Text] = {
    for {
      roomOpt <- manager.get.map(_.getRoom(manager)).flatten
      resp <- manager.modify { m =>
        val gameStateOrError = for {
          _ <- m.playersReady.find(_.id == id).toRight("player is not ready")
          _ <- Either.cond(
            m.playersReady.length >= 2, 
            (), 
            "at least 2 people should be ready"
          )
          room <- roomOpt.toRight(
            "can't find room for this manager, data is unsynchronised"
          )
          _ <- Either.cond(
            room.owner == id,
            (),
            "only owner can start game"
          )
        } yield GameState.startGame[F](m.playersReady, room.mode)

        (
          m, 
          gameStateOrError match {
            case Left(error) => CF.delay(Text(Error(error).asJson.noSpaces))
            case Right(gameState) => 
              for {
                gameState <- gameState
                _ <- manager.update(_.copy(gameState = gameState)) 
              } yield Text(OK.apply.asJson.noSpaces)
          }
        )
      }.flatten
    } yield resp
  }

  def createConnection [F[_] : Concurrent : Timer] (
    manager: Ref[F, GameManager[F]], 
    player: Player
  )(implicit CF: ConcurrentEffect[F]): F[Response[F]] = {

    val echoReply: Pipe[F, WebSocketFrame, WebSocketFrame] =
      _.evalMap{
        case Text(msg, _) => decode[Action](msg) match {
          case Left(error) => CF.delay(Text(Error(error.getMessage()).asJson.noSpaces))
          case Right(action) => action match {
            case MarkReady(_, playerId) => markReady(manager, playerId)
            case StartGame(_, playerId) => startGame(manager, playerId)
          }
        }
        
        case _ => CF.delay(Text("unsupported"))
      }

    val playerNotifier = 
      Stream.repeatEval(
        manager.get.map(m => m.gameState.map(
          state => GameStateMessage.of(state, player).asJson.noSpaces
        ))
      ).collect{ case Some(json) => Text(json) }.metered(1.second)

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
