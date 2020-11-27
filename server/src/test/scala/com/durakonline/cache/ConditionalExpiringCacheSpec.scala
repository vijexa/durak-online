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
    val cache = evaluateCache(_ => false)

    cache.get(0).unsafeRunSync() shouldBe None
    cache.get(1).unsafeRunSync() shouldBe Some("bar")
  }

  it should "redeem expired values using redeem function" in {
    val cache = evaluateCache(_ == "foo")

    cache.get(0).unsafeRunSync() shouldBe Some("foo")
    cache.get(1).unsafeRunSync() shouldBe Some("bar")
  }

  "ConditionalExpiringCache.modify" should "work as expected" in {
    val cache = for {
      cache <- ConditionalExpiringCache.of[IO, Int, String](50.milli, 10.milli)
      _ <- cache.put(0, "foo")
      _ <- cache.put(1, "expiring")
      _ <- IO.sleep(40.milli)
      _ <- cache.modify(0, (_ + "bar"))
      _ <- IO.sleep(40.milli)
      _ <- cache.modify(2, (_ + "test"))
    } yield cache

    cache.unsafeRunSync().get(0).unsafeRunSync() shouldBe Some("foobar")
    cache.unsafeRunSync().get(1).unsafeRunSync() shouldBe None
    cache.unsafeRunSync().get(2).unsafeRunSync() shouldBe None
  }


  // run method needs to be defined when extending IOApp
  def run(args: List[String]): IO[ExitCode] = ???
}
