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

  def evaluateCache (redeemer: (Int, String) => Boolean) = {
    val cache = for {
      cache <- ConditionalExpiringCache.of[IO, Int, String](50.milli, 10.milli, redeemer)
      _ <- cache.put(0, "foo")
      _ <- IO.sleep(50.milli)
      _ <- cache.put(1, "bar")
      _ <- IO.sleep(20.milli)
    } yield cache

    cache.unsafeRunSync()
  }

  "ConditionalExpiringCache" should "remove expired values" in {
    val cache = evaluateCache{
      case (key, value) => false
    }

    cache.get(0).unsafeRunSync() shouldBe None
    cache.get(1).unsafeRunSync() shouldBe Some("bar")
  }

  it should "redeem expired values using redeem function" in {
    val cache = evaluateCache{
      case (key, value) => value == "foo"
    }

    cache.get(0).unsafeRunSync() shouldBe Some("foo")
    cache.get(1).unsafeRunSync() shouldBe Some("bar")
  }


  // run method needs to be defined when extending IOApp
  def run(args: List[String]): IO[ExitCode] = ???
}
