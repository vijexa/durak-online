package com.example.durakonline

import cats.implicits._
import cats.effect.{ConcurrentEffect, Timer}
import fs2.Stream
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import scala.concurrent.ExecutionContext.global

object DurakonlineServer {

  def stream[F[_] : ConcurrentEffect : Timer]: Stream[F, Nothing] = {
    val httpApp = (
      DurakonlineRoutes.helloWorldRoutes[F]
    ).orNotFound

    for {

      // Combine Service Routes into an HttpApp.
      // Can also be done via a Router if you
      // want to extract a segments not checked
      // in the underlying routes.

      // With Middlewares in place

      exitCode <- BlazeServerBuilder[F](global)
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(httpApp)
        .serve
    } yield exitCode
  }.drain
}
