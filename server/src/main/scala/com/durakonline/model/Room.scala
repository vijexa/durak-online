package com.durakonline.model

import com.durakonline.cache.ConditionalExpiringCache

import eu.timepit.refined.auto._
import scala.concurrent.duration._
import cats.effect.Clock
import cats.effect.Timer
import cats.effect.Concurrent
import cats.implicits._

final case class Room [F[_]] private (
  name: RoomName, 
  password: RoomPassword, 
  players: ConditionalExpiringCache[F, UUIDString, Player]
)

final object Room {

  def apply [F[_] : Clock : Timer : Concurrent](
    name: RoomName, 
    password: RoomPassword
  ): F[Room[F]] = {
    for {
      cache <- ConditionalExpiringCache.of[F, UUIDString, Player](
        10.minutes,
        1.minute
      )
    } yield Room(
      name,
      password,
      cache
    )
  }

}