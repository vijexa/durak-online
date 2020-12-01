package com.durakonline.model

import cats.implicits._

import cats.effect.Concurrent
import cats.effect.concurrent.Ref

import eu.timepit.refined.auto._

final case class Lobby private (
  rooms: Map[RoomName, Room]
) {
  def addPlayerToRoom (player: Player, roomName: RoomName): Option[Lobby] = 
    rooms.find{ case (name, _) => name == roomName }
      .map{ case (_, room) => room.addPlayer(player) }
      .map(room => 
        this.copy(rooms = rooms - roomName + (roomName -> room))
      )

  def addRoom (roomName: RoomName, password: RoomPassword): Option[Lobby] = 
    rooms.find{ case (name, _) => name == roomName }
      match {
        case None => 
          this.copy(
            rooms = rooms + (roomName -> Room(roomName, password, Map.empty))
          ).some
        case Some(_) => None
      }
}

object Lobby {
  def of [F[_] : Concurrent]: F[Ref[F, Lobby]] = 
    Ref.of[F, Lobby](
      Lobby (
        Map[RoomName, Room](
          // players that are not yet in a room will be stored inside special
          // "lobby" room
          ("lobby": RoomName) -> Room("lobby", "", Map.empty)
        )
      )
    )
}