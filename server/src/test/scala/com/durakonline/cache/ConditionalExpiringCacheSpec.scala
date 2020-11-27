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

  "A cache" should "work as expected" in {
    val cache = for {
      cache <- ConditionalExpiringCache.of[IO, Int, String](10.milli, 5.milli)
      _ <- cache.put(0, "foo")
      _ <- IO.sleep(10.milli)
      _ <- cache.put(1, "bar")
      _ <- IO.sleep(5.milli)
    } yield cache

    cache.unsafeRunSync().get(0).unsafeRunSync() shouldBe None
    cache.unsafeRunSync().get(1).unsafeRunSync() shouldBe Some("bar")
  }

  // run method needs to be defined when extending IOApp
  def run(args: List[String]): IO[ExitCode] = ???
}
