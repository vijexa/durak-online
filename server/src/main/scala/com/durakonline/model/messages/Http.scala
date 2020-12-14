package com.durakonline.model.messages

import com.durakonline.model._
import com.durakonline.game.GameMode
import com.durakonline.game.GameMode._

import eu.timepit.refined.auto._

import io.circe.generic.JsonCodec
import io.circe.refined._
import io.circe.{ Decoder, Encoder, Json }

import scala.util.Try

object Http {

  object Codecs {
    implicit val encodeGameState: Encoder[GameMode] = new Encoder[GameMode] {
      def apply (a: GameMode): Json = a match {
        case LobbyMode => Json.fromString("lobby")
        case DeckOf24 => Json.fromString("of24")
        case DeckOf36 => Json.fromString("of36")
        case DeckOf52 => Json.fromString("of52")
      }
    }

    implicit val decodeGameMode: Decoder[GameMode] = Decoder.decodeString.emapTry{str =>
      Try(
        str match {
          case "of24" => DeckOf24
          case "of36" => DeckOf36
          case "of52" => DeckOf52
        }
      )
    }
  }
  object Request {
    import Codecs._

    @JsonCodec case class RefinedTest (id: UUIDString, password: RoomPassword)    

    @JsonCodec case class CreatePlayer (
      name: UserName
    )

    @JsonCodec case class CreateRoom (
      name: RoomName, 
      password: RoomPassword,
      mode: GameMode
    )

    @JsonCodec case class RemoveRoom (
      name: RoomName, 
      password: RoomPassword
    )

    @JsonCodec case class JoinRoom (
      roomName: RoomName, 
      roomPassword: RoomPassword
    )
  }

  object Response {
    import Codecs._

    @JsonCodec case class HelloWorld (test: String, hello: String)

    @JsonCodec case class OK private  (status: ResponseStatus)
    object OK {
      def apply = new OK("ok")
    }

    @JsonCodec case class Error private  (
      status: ResponseStatus, 
      errorDescription: ErrorDescription
    )
    object Error {
      def apply (errorDescription: String) = new Error("error", errorDescription)
    }

    @JsonCodec case class RoomData (
      roomName: RoomName, 
      mode: GameMode, 
      playerCount: Int
    )
    object RoomData {
      def apply (room: Room): RoomData = 
        RoomData(room.name, room.mode, room.players.size)
    }
    
    @JsonCodec case class RoomsList (rooms: List[RoomData])
    object RoomsList {
      def apply (lobby: Lobby): RoomsList = 
        RoomsList(lobby.rooms.values.map(RoomData.apply).toList)
    }

    @JsonCodec case class PlayersList (players: List[UserName])
    object PlayersList {
      def apply (lobby: Lobby): PlayersList = 
        PlayersList(lobby.getAllPlayers.values.map(_.name).toList)
    }
  }
}
