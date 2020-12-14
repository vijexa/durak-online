package com.durakonline.model

import cats.implicits._

import cats.effect.Concurrent
import cats.effect.concurrent.Ref

import eu.timepit.refined.auto._
import com.durakonline.game.GameMode

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
    * @param roomPassword
    * @return
    */
  def addPlayerToRoom (
    player: Player, 
    roomName: RoomName,
    roomPassword: RoomPassword
  ): Either[ErrorDescription, Lobby] = 
    for {
      _       <- getPlayer(player.id).fold(
        er => Right(er), 
        _ => s"player with this id already exists".asLeft
      )

      room    <- getRoom(roomName)

      _       <- Either.cond(
        room.password == roomPassword,
        (),
        "wrong password"
      )

      newRoom <- room.addPlayer(player)
    } yield this.copy(rooms = rooms + (roomName -> newRoom))

  /**
    * Returns lobby, where specified player was removed from whichever room it
    * was in, if any, and then put into specified room
    *
    * @param player
    * @param roomName
    * @param roomPassword
    * @return
    */
  def movePlayerToRoom (
    player: Player,
    roomName: RoomName,
    roomPassword: RoomPassword
  ): Either[ErrorDescription, Lobby] = 
    removePlayer(player.id)
      .addPlayerToRoom(player, roomName, roomPassword)

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
    * @param owner
    * @return
    */
  def addRoom (
    roomName: RoomName, 
    password: RoomPassword,
    owner: UUIDString,
    mode: GameMode
  ): Either[ErrorDescription, Lobby] = 
    getRoom(roomName) match {
        // create new room if there is no room with such name
        case Left(_)  => 
          this.copy(
            rooms = rooms + (roomName -> Room(
              roomName, 
              password, 
              owner, 
              Map.empty, 
              mode
            ))
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
    * @param password
    * @param userId
    * @return
    */
  def removeRoom (
    roomName: RoomName, 
    password: RoomPassword, 
    userId: UUIDString
  ): Either[ErrorDescription, Lobby] = {
    // TODO: maybe make it return either
    if (roomName.value != "lobby") {
      
      val newRooms = rooms.filterNot { 
        case (name, room) => 
          name == roomName && 
          room.owner == userId && 
          room.password == password
      }
      
      if (newRooms.size == rooms.size) {
        Left("room removal wasn't succesful")
      } else this.copy(rooms = newRooms).asRight
      
    } else Left("cannot remove this room")
  }
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
          ("lobby": RoomName) -> Room(
            "lobby", 
            "", 
            // placeholder until I decide what to do with this 
            "9430e584-3a8b-4b92-ad6a-ef3d75bea3a5",
            Map.empty,
            GameMode.LobbyMode
          )
        )
      )
    )
}