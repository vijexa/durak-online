package com.durakonline.model.messages

import com.durakonline.model._

import eu.timepit.refined.auto._

import io.circe.generic.JsonCodec
import io.circe.refined._

object Http {
  object Request {
    @JsonCodec case class RefinedTest (id: UUIDString, password: RoomPassword)    

    @JsonCodec case class CreatePlayer (
      name: UserName
    )

    @JsonCodec case class CreateRoom (
      name: RoomName, 
      password: RoomPassword
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
  }
}
