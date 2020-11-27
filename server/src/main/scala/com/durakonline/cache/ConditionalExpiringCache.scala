package com.durakonline.cache

import scala.concurrent.duration.FiniteDuration

import cats.Monad
import cats.effect._
import cats.effect.concurrent.Ref
import cats.implicits._

class ConditionalExpiringCache[F[_] : Clock : Monad, K, V](
  state: Ref[F, Map[K, (Long, V)]],
  expiresIn: FiniteDuration
) extends Cache[F, K, V] {

  def checkExpirationRepeatedly(
    interval: FiniteDuration
  )(
    implicit T: Timer[F], C: Concurrent[F]
  ): F[Unit] = {
    for {
      _ <- state.update(
        _.collect{
          case (key, (exp, value)) if exp > 0 => (key, (exp - interval.length, value))
        }
      )
      _ <- T.sleep(interval)
      _ <- checkExpirationRepeatedly(interval)
    } yield ()
  }

  def get(key: K): F[Option[V]] = state.get.map(_.get(key).map{case (_, v) => v})

  def put(key: K, value: V): F[Unit] = state.update(
    m => (
      m + (key -> (expiresIn.length -> value))
    )
  )

  def remove(key: K): F[Unit] = state.update(
    m => (
      m.removed(key)
    )
  )

}

object ConditionalExpiringCache {
  def of[F[_] : Clock, K, V](
    expiresIn: FiniteDuration,
    checkOnExpirationsEvery: FiniteDuration
  )(implicit T: Timer[F], C: Concurrent[F]): F[Cache[F, K, V]] = {

    for {
      cache <- C.delay(
        new ConditionalExpiringCache[F, K, V](
          Ref.unsafe[
            F, 
            Map[K, (Long, V)]
          ](Map.empty),
          expiresIn
        )
      )
      _ <- C.start(
        cache.checkExpirationRepeatedly(
          checkOnExpirationsEvery
        )
      )
    } yield cache
  }
    

}