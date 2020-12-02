package com.durakonline.model

final case class Room (
  name: RoomName, 
  password: RoomPassword,
  owner: UUIDString,
  players: Map[UUIDString, Player]
) {
  /**
    * Returns room with added player, or an error description
    *
    * @param player
    * @return
    */
  def addPlayer (player: Player): Either[ErrorDescription, Room] = 
    players.find{ case (_, currPlayer) => currPlayer.id == player.id }
      match {
        case None    => 
          Right(this.copy(players = players + (player.id -> player)))
        case Some(_) => 
          Left(s"player with such id already exists in room $name")
      }

  /**
    * Returns room with removed player, if any
    *
    * @param id
    * @return
    */
  def removePlayer (id: UUIDString): Room = 
    this.copy(players = players.filter{ case (currId, _) => currId != id })
}

final object Room {

}