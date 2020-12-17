package com.durakonline.game.network

import cats.effect._
import cats.syntax.all._
import fs2._
import fs2.concurrent.Queue
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.websocket._
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame._
import scala.concurrent.duration._

class WebsocketRoutes[F[_]](
  implicit F: ConcurrentEffect[F], 
  timer: Timer[F]
) extends Http4sDsl[F] {

  def routes: HttpRoutes[F] =
    HttpRoutes.of[F] {

      // modified echo example to understand how strems work...
      case GET -> Root / "wsecho" =>
        val echoReply: Pipe[F, WebSocketFrame, WebSocketFrame] =
          _.collect {
            case Text(msg, _) => Text("You sent the server: " + msg)
            case _ => Text("Something new")
          }
      
        val pingStream = 
          Stream.awakeEvery[F](1.seconds).map(d => Text(s"Ping! $d"))

        Queue
          .unbounded[F, WebSocketFrame]
          .flatMap { q =>
            WebSocketBuilder[F].build(
              receive = q.enqueue,
              send = q.dequeue.through(echoReply) merge pingStream
            )
          }

      case req @ GET -> Root / "room-connect" =>
        req.cookies.find(_.name == "id").fold(
          BadRequest
        ){id =>
          ???
        }
        ???
    }
}

object WebsocketRoutes {
  def apply[F[_]: ConcurrentEffect: Timer]: WebsocketRoutes[F] =
    new WebsocketRoutes[F]
}
