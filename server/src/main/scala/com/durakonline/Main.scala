package com.durakonline

import cats.effect.{IO, IOApp}

object Main extends IOApp {
  def run(args: List[String]) =
    HttpServer.run[IO]
}
