package com.durakonline.game.network

import com.durakonline.model._

import eu.timepit.refined.auto._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.string._

import io.circe.generic.JsonCodec
import io.circe.refined._

import cats.syntax.functor._
import io.circe.{ Decoder, Encoder }, io.circe.generic.auto._
import io.circe.syntax._

object Messages {
  object HelperTypes {
    type MarkReadyString = String Refined MatchesRegex["mark-ready"]
  }

  object Request {
    import HelperTypes._

    sealed trait Action

    case class MarkReady(
      action: MarkReadyString, 
      playerId: UUIDString
    ) extends Action

    implicit val encodeEvent: Encoder[Action] = Encoder.instance {
      case j : MarkReady => j.asJson
    }

    implicit val decodeEvent: Decoder[Action] =
      List[Decoder[Action]](
        Decoder[MarkReady].widen
      ).reduceLeft(_ or _)
  }

  object Response {
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
  }
}
