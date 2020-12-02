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
    * 
    * @return
    */
  def getAllPlayers: Map[UUIDString, Player] =
    rooms.flatMap{ case (_, room) => room.players }

  /**
    * Returns a player with specified id, or an error
    *
    * @param id
    * @return
    */
  def getPlayer (id: UUIDString): Either[ErrorDescription, Player] =
    getAllPlayers.get(id) match {
      case Some(player) => player.asRight
      case None         => "there is no player with specified id".asLeft
    }

  /**
    * Returns lobby with player added to the room, or an error description
    *
    * @param player
    * @param roomName
    * @return
    */
  def addPlayerToRoom (
    player: Player, 
    roomName: RoomName
  ): Either[ErrorDescription, Lobby] = 
    for {
      _       <- getPlayer(player.id).fold(
        er => Right(er), 
        _ => s"player with this id already exists".asLeft
      )
      room    <- getRoom(roomName)
      newRoom <- room.addPlayer(player)
    } yield this.copy(rooms = rooms + (roomName -> newRoom))

  /**
    * Returns lobby, where specified player was removed from whichever room it
    * was in, if any, and then put into specified room
    *
    * @param player
    * @param roomName
    * @return
    */
  def movePlayerToRoom (
    player: Player,
    roomName: RoomName
  ): Either[ErrorDescription, Lobby] = 
    removePlayer(player.id)
      .addPlayerToRoom(player, roomName)  

  /**
    * Returns lobby, where player with specified id was removed from whichever
    * room it was in, if any
    *
    * @param playerId
    * @return
    */
  def removePlayer (
    playerId: UUIDString
  ): Lobby = {
    for {
      (roomName, room) <- 
        rooms.find{ case (_, room) => room.players.get(playerId).isDefined }
      newRoom = room.removePlayer(playerId)

    } yield this.copy(rooms = rooms - roomName + (roomName -> newRoom))
  }.getOrElse(this)

  /**
    * Returns lobby with added room, or an error description
    *
    * @param roomName
    * @param password
    * @return
    */
  def addRoom (
    roomName: RoomName, 
    password: RoomPassword
  ): Either[ErrorDescription, Lobby] = 
    getRoom(roomName) match {
        // create new room if there is no room with such name
        case Left(_)  => 
          this.copy(
            rooms = rooms + (roomName -> Room(roomName, password, Map.empty))
          ).asRight

        case Right(_) => s"room with name $roomName already exists".asLeft
      }

  /**
    * Returns a room with specified name, or an error description
    *
    * @param roomName
    * @return
    */
  def getRoom (roomName: RoomName): Either[ErrorDescription, Room] = 
    rooms.get(roomName) match {
      case Some(room) => room.asRight
      case None       => s"there is no room with name $roomName".asLeft
    }

  /**
    * Returns a lobby with specified room removed, if any
    *
    * @param roomName
    * @return
    */
  def removeRoom (roomName: RoomName): Lobby =
    if (roomName.value != "lobby")
      this.copy(rooms = rooms.filter{ case (name, _) => name == roomName })
    else this
}

object Lobby {
  /**
    * Creates new lobby with one empty 'lobby' room, and returns a Ref to it
    *
    * @return
    */
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