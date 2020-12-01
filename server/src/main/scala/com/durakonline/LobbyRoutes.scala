package com.durakonline

import com.durakonline.model.messages.Http._
import com.durakonline.model._

import cats.implicits._
import cats.effect.Sync
import cats.effect.concurrent.Ref

import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.circe._

import io.circe.syntax._

import eu.timepit.refined.auto._

import java.util.UUID
import eu.timepit.refined.api.RefType

class LobbyRoutes [F[_]: Sync] (state: Ref[F, Lobby]) {
  val dsl = new Http4sDsl[F]{}
  import dsl._

  def helloWorldRoutes: HttpRoutes[F] = {
    HttpRoutes.of[F] {
      case GET -> Root / "hello" / name =>
        Ok(Response.HelloWorld("testing stuff", name).asJson)
    }
  }

  def refinedTestRoutes: HttpRoutes[F] = {
    HttpRoutes.of[F] {
      case req @ POST -> Root / "refined-test" =>
        req.decodeJson[Request.RefinedTest].flatMap { message =>
          implicitly[Sync[F]].delay(println(s"\n\nid: ${message.id}, pwd: ${message.password}\n\n")) *>
          Ok("nice")
        }
    }
  }

  def roomManagementRoutes: HttpRoutes[F] = {
    HttpRoutes.of[F] {
      case req @ POST -> Root / "create-room" =>
        req.decodeJson[Request.CreateRoom].flatMap { message => 
          
          for {
            newState <- state.modify(
              lobby => {
                val lobbyOpt = lobby.addRoom(message.name, message.password)
                (lobbyOpt.getOrElse(lobby), lobbyOpt)
              }
            )

            response <- newState.fold(
              Ok(Response.Error("failed to create room").asJson)
            )(_ =>
              Ok(Response.OK.apply.asJson) 
            )
          } yield response
        }
    }
  }

  def playerManagementRoutes: HttpRoutes[F] = {
    HttpRoutes.of[F] {

      case req @ POST -> Root / "new-player" =>
        req.decodeJson[Request.CreatePlayer].flatMap { message =>
          
          for {
            newState <- state.updateAndGet(lobby =>
              lobby.addPlayerToRoom(
                Player(
                  RefType.applyRef[UUIDString](UUID.randomUUID().toString()).toOption.get,
                  message.name
                ), 
                "lobby"
              ).getOrElse(lobby)
            )  

            resp <- Ok(s"new state is $newState")
          } yield resp
          
        }
    }
  }
}