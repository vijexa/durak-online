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
          implicitly[Sync[F]].delay(
            println(s"\n\nid: ${message.id}, pwd: ${message.password}\n\n")
          ) *>
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
                val lobbyEither = for {    
                  userId <- req.cookies.find(_.name == "id") match {
                    case Some(value) => 
                      RefType.applyRef[UUIDString](value.content)
                    case None => Left("no id in cookies")
                  }

                  newLobby <- lobby.addRoom(
                    message.name, 
                    message.password, 
                    userId
                  )
                } yield newLobby
                (lobbyEither.getOrElse(lobby), lobbyEither)
              }
            )

            response <- newState.fold(
              error => 
                Ok(Response.Error(s"failed to create room: $error").asJson),
              _     => 
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
            newState <- state.modify(
              lobby => {
                val newId = UUID.randomUUID().toString()
                val lobbyOpt = lobby.addPlayerToRoom(
                  Player(
                    RefType.applyRef[UUIDString](newId).toOption.get,
                    message.name
                  ), 
                  "lobby"
                )

                (lobbyOpt.getOrElse(lobby), (newId -> lobbyOpt))
              }
            )  

            resp <- newState match {
              case (id, Right(lobby)) => 
                Ok(Response.OK.apply.asJson).map(_.addCookie("id", id))
              case (_,  Left(error))  => 
                Ok(
                  Response.Error(s"could not create new player: $error").asJson
                )
            }
          } yield resp
          
        }
    }
  }

  val superSecretUrl = "291a7418-aec6-4c4d-9455-e9673f41dcb7-" +
    "92ad1baa-f5a5-4534-a4b6-1d2df5989a84"
  def checkStateDebug: HttpRoutes[F] = HttpRoutes.of[F] {

    // hacky way to check out current state with a secret 
    // url, shouldn't be exposed on production
    case GET -> Root / "debug" / "lobby-state" / superSecretUrl => {
      val pprintBW = pprint.PPrinter.BlackWhite
      for {
        lobby <- state.get
        
        formatted = pprintBW.apply(lobby)

        resp <- Ok(s"Current state is $formatted")
      } yield resp
    }

  }
}