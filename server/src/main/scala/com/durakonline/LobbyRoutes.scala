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
import io.circe.Decoder

import eu.timepit.refined.auto._

import java.util.UUID

import eu.timepit.refined.api.RefType
import com.durakonline.game.GameManager
import cats.effect.Concurrent

// TODO: REFACTOR THIS HORRIFIC NIGHTMARE


class LobbyRoutes [F[_]: Sync : Concurrent] (state: Ref[F, Lobby[F]]) {
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

  def modifyStateFromJsonAndReturnResponse[J : Decoder](
    req: org.http4s.Request[F],
    modifier: (Lobby[F], UUIDString, J) => Either[ErrorDescription, Lobby[F]],
    errorMessage: ErrorDescription
  ) = 
  req.decodeJson[J].flatMap{ message => 
    for {
      newState <- state.modify{ lobby =>
        val lobbyEither = for {
          userId <- req.cookies.find(_.name == "id") match {
            case Some(value) => 
              RefType.applyRef[UUIDString](value.content)
            case None => Left("no id in cookies")
          }

          newLobby <- modifier(lobby, userId, message)
        } yield newLobby

        (lobbyEither.getOrElse(lobby), lobbyEither)
      }

      response <- newState.fold(
        error =>
          Ok(Response.Error(errorMessage + error).asJson),
        _ =>
          Ok(Response.OK.apply.asJson)
      )
    } yield response
  }

  def roomManagementRoutes: HttpRoutes[F] = {
    HttpRoutes.of[F] {

      case req @ POST -> Root / "create-room" => 
        for {
          manager <- GameManager.empty[F]
          resp <- modifyStateFromJsonAndReturnResponse [Request.CreateRoom] (
            req, 
            (lobby, id, message) => lobby.addRoom(
              message.name, 
              message.password, 
              id,
              message.mode,
              manager
            ),
            "failed to create room: "
          )
        } yield resp
        

      case req @ POST -> Root / "remove-room" =>
        modifyStateFromJsonAndReturnResponse [Request.RemoveRoom] (
          req, 
          (lobby, id, message) => lobby.removeRoom(
            message.name, 
            message.password, 
            id
          ),
          "failed to remove room: "
        )

      case req @ GET -> Root / "all-rooms" =>
        for {
          lobby <- state.get
          response <- Ok(Response.RoomsList(lobby).asJson)
        } yield response

      case req @ GET -> Root / "get-all-player-names" =>
        for {
          lobby <- state.get
          response <- Ok(Response.PlayersList(lobby).asJson)
        } yield response
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
                  "lobby",
                  ""
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

      case req @ POST -> Root / "join-room" => 
        modifyStateFromJsonAndReturnResponse [Request.JoinRoom] (
          req, 
          (lobby, id, message) => {
            for {
              player   <- lobby.getPlayer(id)
              newLobby <- lobby.movePlayerToRoom(
                player,
                message.roomName, 
                message.roomPassword
              )
            } yield newLobby
          },
          "failed to join room: "
        )
    }
  }

  val superSecretUrl = "291a7418-aec6-4c4d-9455-e9673f41dcb7-" +
    "92ad1baa-f5a5-4534-a4b6-1d2df5989a84"
  def checkStateDebug: HttpRoutes[F] = HttpRoutes.of[F] {

    // hacky way to check out current state with a "secret" 
    // url, shouldn't be exposed on production
    // will be removed after I'm done here
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