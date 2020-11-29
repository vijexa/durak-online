package com.durakonline.cache

import scala.concurrent.duration.FiniteDuration

import cats.Monad
import cats.effect._
import cats.effect.concurrent.Ref
import cats.implicits._

/**
  * Expiring cache that can redeem expired elements.
  */
class ConditionalExpiringCache[F[_] : Clock : Monad, K, V] private (
  state: Ref[F, Map[K, (Long, V)]],
  expiresIn: FiniteDuration
) extends Cache[F, K, V] {

  private def checkExpirationRepeatedly(
    interval: FiniteDuration,
    redeemer: V => Boolean
  )(
    implicit T: Timer[F], C: Concurrent[F]
  ): F[Unit] = {
    for {
      _ <- state.update(
        _.collect{
          // collect only values that were not expired
          case (key, (exp, value)) if exp > 0 => 
            (key, (exp - interval.length, value))
          // if value is expired it can still be collected if redeemer returns true
          case (key, (exp, value)) if exp <= 0 && redeemer(value) => 
            (key, (expiresIn.length, value))
        }
      )
      _ <- T.sleep(interval)
      _ <- checkExpirationRepeatedly(interval, redeemer)
    } yield ()
  }

  /**
    * Returns `Option` of value by key.
    */
  def get(key: K): F[Option[V]] = state.get.map(_.get(key).map{case (_, v) => v})

  /**
    * Puts new value at specified key. 
    */
  def put(key: K, value: V): F[Unit] = state.update(
    m => (
      m + (key -> (expiresIn.length -> value))
    )
  )

  /**
    * Atomically modify value if it exists, using `f` function.
    * Resets value expiration time.
    */
  def modify(key: K, f: V => V) = state.update(
    m => m.get(key)
      .map{case (_, value) => f(value)}
      .fold(m)(
        value => m + (
          key -> (expiresIn.length -> value)
        )
      )
  )

  /**
    * Removes value at specified key.
    */
  def remove(key: K): F[Unit] = state.update(
    m => (
      m.removed(key)
    )
  )

  /**
    * Returns amount of elements in cache
    */
  def size = state.get.map(_.size)

}

/**
  * [[ConditionalExpiringCache]] companion object
  */
object ConditionalExpiringCache {
  // default redeemer always fails
  private def defaultRedeemer [V] (value: V) = false

  /**
    * @param expiresIn Time in which newly added element is expired.
    * @param checkOnExpirationsEvery Interval for checking elements expiration.
    * @param redeemer Function that is applied to expired elements. 
    * If it returns true, then this element will not be removed and
    * its expiration will be postponed for another `expiresIn`.
    * @param T
    * @param C
    * @return
    */
  def of[F[_] : Clock, K, V](
    expiresIn: FiniteDuration,
    checkOnExpirationsEvery: FiniteDuration,
    redeemer: V => Boolean = defaultRedeemer _
  )(implicit T: Timer[F], C: Concurrent[F]): F[ConditionalExpiringCache[F, K, V]] = {

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
          checkOnExpirationsEvery,
          redeemer
        )
      )
    } yield cache
  }

}