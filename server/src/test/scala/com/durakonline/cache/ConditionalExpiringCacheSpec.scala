package com.durakonline.cache

import cats.effect.IO
import cats.effect.ExitCode
import cats.effect.IOApp
import scala.concurrent.duration._
import com.durakonline.cache.ConditionalExpiringCache

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._

// extending IOApp to have those sweet implicits
class ConditionalExpiringCacheSpec extends AnyFlatSpec with IOApp {

  def evaluateCache (redeemer: String => Boolean) = {
    for {
      cache <- ConditionalExpiringCache.of[IO, Int, String](50.milli, 10.milli, redeemer)
      _ <- cache.put(0, "foo")
      _ <- IO.sleep(50.milli)
      _ <- cache.put(1, "bar")
      _ <- IO.sleep(20.milli)
    } yield cache
  }

  "ConditionalExpiringCache" should "remove expired values" in {
    val test = for {
      cache <- evaluateCache(_ => false)
      zero  <- cache.get(0)
      _     <- IO(zero shouldBe None)
      one   <- cache.get(1)
      _     <- IO(one shouldBe Some("bar"))
      size  <- cache.size
      _     <- IO(size shouldBe 1)
    } yield ()

    test.unsafeRunSync()
  }

  it should "redeem expired values using redeem function" in {
    
    val test = for {
      cache <- evaluateCache(_ == "foo")
      zero  <- cache.get(0)
      _     <- IO(zero shouldBe Some("foo"))
      one   <- cache.get(1)
      _     <- IO(one shouldBe Some("bar"))
      size  <- cache.size
      _     <- IO(size shouldBe 2)
    } yield ()

    test.unsafeRunSync()
  }

  "ConditionalExpiringCache.modify" should "work as expected" in {
    val test = for {
      cache <- ConditionalExpiringCache.of[IO, Int, String](50.milli, 10.milli)
      _ <- cache.put(0, "foo")
      _ <- cache.put(1, "expiring")
      _ <- IO.sleep(40.milli)
      _ <- cache.modify(0, (_ + "bar"))
      _ <- IO.sleep(40.milli)
      _ <- cache.modify(2, (_ + "test"))

      zero <- cache.get(0)
      _    <- IO(zero shouldBe Some("foobar"))
      one  <- cache.get(1)
      _    <- IO(one shouldBe None)
      two  <- cache.get(2)
      _    <- IO(two shouldBe None)
    } yield cache

    test.unsafeRunSync()
  }


  // run method needs to be defined when extending IOApp
  def run(args: List[String]): IO[ExitCode] = ???
}
