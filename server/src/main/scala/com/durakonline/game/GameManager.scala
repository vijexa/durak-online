package com.durakonline.game

import com.durakonline.model._

import org.http4s.Response
import cats.effect.Sync


import cats.effect._
import cats.syntax.all._
import cats.effect.concurrent.Ref

import fs2._
import fs2.concurrent.Queue

import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.websocket._
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame._

import scala.concurrent.duration._
import eu.timepit.refined.api.RefType

case class GameManager [F[_] : Sync : Concurrent] (
  roomName: RoomName,
  gameState: Option[GameState],
  players: Vector[Player],
  playersReady: Vector[Player]
) {
  def addPlayer (player: Player): GameManager[F] =
    copy(players = players :+ player)

  def createConnection (player: Player): F[Response[F]] = {
    val echoReply: Pipe[F, WebSocketFrame, WebSocketFrame] =
      _.collect {
        case Text(msg, _) => Text("You sent the server: " + msg + " and players are " + players)
        case _ => Text("Something new")
      }

    Queue
      .unbounded[F, WebSocketFrame]
      .flatMap { q =>
        WebSocketBuilder[F].build(
          receive = q.enqueue,
          send = q.dequeue.through(echoReply)
        )
      }
  }
}

object GameManager {

}
