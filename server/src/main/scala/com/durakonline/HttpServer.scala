package com.durakonline

import cats.effect.{ConcurrentEffect, Timer}
import fs2.Stream
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import scala.concurrent.ExecutionContext.global

object HttpServer {

  def stream[F[_] : ConcurrentEffect : Timer]: Stream[F, Nothing] = {
    val httpApp = (
      LobbyRoutes.helloWorldRoutes[F]
    ).orNotFound

    for {
      exitCode <- BlazeServerBuilder[F](global)
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(httpApp)
        .serve
    } yield exitCode
  }.drain
}
