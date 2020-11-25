package com.example.durakonline

import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp {
  def run(args: List[String]) =
    HttpServer.stream[IO].compile.drain.as(ExitCode.Success)
}
