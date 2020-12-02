package com.durakonline.model

import cats.implicits._

import cats.effect.Concurrent
import cats.effect.concurrent.Ref

import eu.timepit.refined.auto._

final case class Lobby private (
  rooms: Map[RoomName, Room]
) {
  /**
    * Returns a flattened Map of all players in all rooms
    */
  def getAllPlayers: Map[UUIDString, Player] =
    rooms.flatMap{ case (_, room) => room.players }

  def getPlayer (id: UUIDString): Either[ErrorDescription, Player] =
    getAllPlayers.get(id) match {
      case Some(player) => player.asRight
      case None         => Left(s"there is no player with specified id")
    }

  def addPlayerToRoom (player: Player, roomName: RoomName): Either[ErrorDescription, Lobby] = 
    for {
      _       <- getPlayer(player.id).fold(er => Right(er), _ => Left(s"player with this id already exists"))
      room    <- getRoom(roomName)
      newRoom <- room.addPlayer(player)
    } yield this.copy(rooms = rooms - roomName + (roomName -> newRoom))

  def addRoom (roomName: RoomName, password: RoomPassword): Either[ErrorDescription, Lobby] = 
    getRoom(roomName) match {
        // create new room if there is no room with such name
        case Left(_)  => 
          this.copy(
            rooms = rooms + (roomName -> Room(roomName, password, Map.empty))
          ).asRight

        case Right(_) => Left(s"room with name $roomName already exists")
      }

  def getRoom (roomName: RoomName): Either[ErrorDescription, Room] = 
    rooms.get(roomName) match {
      case Some(room) => room.asRight
      case None       => Left(s"there is no room with name $roomName")
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