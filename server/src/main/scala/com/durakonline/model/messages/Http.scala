package com.durakonline.model.messages

import com.durakonline.model._

import io.circe.generic.JsonCodec
import io.circe.refined._

object Http {
  @JsonCodec case class HelloWorldMessage (test: String, hello: String)
  @JsonCodec case class RefinedTestMessage (id: UUIDString, password: RoomPassword)
}
