package com.durakonline

import com.durakonline.model.messages.Http._

import cats.implicits._
import cats.effect.Sync

import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.circe._

import io.circe.syntax._

object LobbyRoutes {

  def helloWorldRoutes[F[_]: Sync]: HttpRoutes[F] = {
    val dsl = new Http4sDsl[F]{}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "hello" / name =>
        Ok(HelloWorldMessage("testing stuff", name).asJson)
    }
  }

  def refinedTestRoutes[F[_]: Sync]: HttpRoutes[F] = {
    val dsl = new Http4sDsl[F]{}
    import dsl._
    HttpRoutes.of[F] {
      case req @ POST -> Root / "refined-test" =>
        req.decodeJson[RefinedTestMessage].flatMap { message =>
          implicitly[Sync[F]].delay(println(s"\n\nid: ${message.id}, pwd: ${message.password}\n\n")) *>
          Ok("nice")
        }
    }
  }
}