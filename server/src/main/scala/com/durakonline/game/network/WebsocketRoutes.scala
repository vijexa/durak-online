package com.durakonline.game.network

import com.durakonline.model._
import com.durakonline.game._

import cats.effect._
import cats.syntax.all._
import cats.effect.concurrent.Ref

import fs2._
import fs2.concurrent.Queue

import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.websocket._
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame._

import scala.concurrent.duration._
import eu.timepit.refined.api.RefType

class WebsocketRoutes[F[_] : Timer](
  lobby: Ref[F, Lobby[F]],
  managers: Ref[F, Map[RoomName, GameManager[F]]]
)(
  implicit F: ConcurrentEffect[F]
) extends Http4sDsl[F] {

  def routes: HttpRoutes[F] =
    HttpRoutes.of[F] {

      // modified echo example to understand how streams work...
      case GET -> Root / "wsecho" =>
        for {
          ref <- Ref.of[F, Int](0)

          echoReply: Pipe[F, WebSocketFrame, WebSocketFrame] =
            _.collect{ case t: Text => t }
              .evalMap {
                case Text(msg, _) => ref.modify(x =>
                  (
                    x + 1,
                    Text(s"#$x You sent the server: $msg")
                  )
                )
              }
        
          pingStream = 
            Stream.awakeEvery[F](1.seconds).map(d => Text(s"Ping! $d"))

          response <- Queue
            .unbounded[F, WebSocketFrame]
            .flatMap { q =>
              WebSocketBuilder[F].build(
                receive = q.enqueue,
                send = q.dequeue.through(echoReply) merge pingStream
              )
            }
        } yield response

      case req @ GET -> Root / "room-connect" / roomName =>
        req.cookies.find(_.name == "id").fold(
          BadRequest("no id in cookies")
        ){idCookie => 
          for {
            lobby <- lobby.get
            manager <- managers.modify { managers =>
              val connectionWithManagers = for {
                id <- RefType.applyRef[UUIDString](idCookie.content)
                name <- RefType.applyRef[RoomName](roomName)
                room <- lobby.rooms.get(name)
                  .toRight("no room with specified name")
                player <- room.players.get(id)
                  .toRight("no player with specified id")

                manager = managers.getOrElse(
                  name, 
                  GameManager[F](name, None, Vector.empty, Vector.empty)
                )
                
                newManager = manager.addPlayer(player)
              } yield (
                newManager, 
                managers - manager.roomName + 
                  (manager.roomName -> newManager)
              )
              
              connectionWithManagers match {
                case Left(value) => (managers, Left(value))
                case Right(value) => value match {
                  case (resp, newManagers) => (newManagers, Right(resp))
                }
              }
            }
            
            response <- manager match {
              case Left(value) => BadRequest(value)
              case Right(manager) => 
                val echoReply: Pipe[F, WebSocketFrame, WebSocketFrame] = stream => {
                  stream.evalMapFilter {
                    case Text(msg, _) => managers.get.map(_.get(manager.roomName).map(manager => 
                      Text("You sent the server: " + msg + " and players are " + manager.players)
                    ))
                  }
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
          } yield response
          
        }
    }
}

