package com.durakonline

import com.durakonline.model._

import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder


import scala.concurrent.ExecutionContext.global

import cats.implicits._

import cats.effect.{ConcurrentEffect, Timer}
import cats.effect.ExitCode

object HttpServer {

  def run[F[_] : ConcurrentEffect : Timer] = {
    
    {
      for {
        lobby <- Lobby.of[F]

        lobbyRoutes = new LobbyRoutes[F](lobby)

        httpApp = (
          lobbyRoutes.helloWorldRoutes <+> 
          lobbyRoutes.refinedTestRoutes <+> 
          lobbyRoutes.roomManagementRoutes <+> 
          lobbyRoutes.playerManagementRoutes <+> 
          lobbyRoutes.checkStateDebug
        ).orNotFound

      } yield for {
        exitCode <- BlazeServerBuilder[F](global)
          .bindHttp(8080, "0.0.0.0")
          .withHttpApp(httpApp)
          .serve
          .compile
          .drain
      } yield ExitCode.Success
    }.flatten
  }
}
