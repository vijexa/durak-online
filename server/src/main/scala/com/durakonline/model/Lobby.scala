package com.durakonline.model

import com.durakonline.cache.ConditionalExpiringCache

import cats.effect._
import cats.implicits._

import scala.concurrent.duration._

final case class Lobby [F[_]] private (
  rooms: ConditionalExpiringCache[F, RoomName, Room]
)

object Lobby {

  def apply [F[_] : Clock : Timer : Concurrent]: F[Lobby[F]] = {
    for {
      cache <- ConditionalExpiringCache.of[F, RoomName, Room](
        10.minutes,
        1.minute
      )
    } yield Lobby(
      cache
    )
  }

}