package com.durakonline.model

import eu.timepit.refined.auto._

import cats.effect._

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._

// yeah it's unreadable but I don't have much time sorryyyyy

class LobbySpec extends AnyFlatSpec with IOApp {

  "Lobby" should "work as expected" in {
    def testAddPlayerToRoomRight (lobby: Lobby, player: Player, roomName: RoomName) =
      lobby.addPlayerToRoom(player, roomName)
        .fold(
          er => fail(s"Lobby.addPlayerToRoom returned unexpected Left: $er"), 
          identity
        )
    
    def testAddPlayerToRoomLeft (lobby: Lobby, player: Player, roomName: RoomName) =
      lobby.addPlayerToRoom(player, "lobby")
        .fold(
          identity, 
          _ => fail("Lobby.addPlayerToRoom returned unexpected Right")
        )

    val uuid: UUIDString = "e79d1f15-11c6-48f0-9575-e323057438b2"
    val player = Player(uuid, "foobar")

    val initial = Lobby.of[IO].unsafeRunSync().get.unsafeRunSync()

    val a = testAddPlayerToRoomRight(initial, player, "lobby")

    a.getAllPlayers.size shouldBe 1
    a.getPlayer(uuid) shouldBe Right(player)

    testAddPlayerToRoomLeft(a, player, "lobby")
    
    val b = a.addRoom("foobar", "foobar123")
      .fold(
        er => fail(s"Lobby.addRoom returned unexpected Left: $er"),
        identity
      )
    
    testAddPlayerToRoomLeft(b, player, "foobar")

    val player2 = Player(
      "b437936d-9e86-44e3-a419-90805c131206", 
      "testplayer"
    )

    val c = testAddPlayerToRoomRight(b, player2,"foobar")

    c.getAllPlayers.size shouldBe 2

    c.addRoom("foobar", "foobar123")
      .fold(
        identity,
        _ => fail("Lobby.addRoom returned unexpected Right")
      )

    val d = c.removePlayer(player.id)

    d.getAllPlayers.size shouldBe 1
    d.getPlayer(player.id).isLeft shouldBe true

    d.rooms("lobby").players.contains(player2.id) shouldBe false
    d.rooms("foobar").players.contains(player2.id) shouldBe true

    val e = d.movePlayerToRoom(player2, "lobby").fold(
      er => fail(s"Lobby.movePlayerToRoom returned unexpected Left: $er"),
      identity
    )

    e.getAllPlayers.size shouldBe 1
    e.rooms("lobby").players.contains(player2.id) shouldBe true
    e.rooms("foobar").players.contains(player2.id) shouldBe false
  }

  def run(args: List[String]): IO[ExitCode] = ???
}
