package com.durakonline.model

final case class Room (
  name: RoomName, 
  password: RoomPassword, 
  players: Map[UUIDString, Player]
) {
  def addPlayer (player: Player): Either[ErrorDescription, Room] = 
    players.find{ case (_, currPlayer) => currPlayer.id == player.id }
      match {
        case None    => 
          Right(this.copy(players = players + (player.id -> player)))
        case Some(_) => 
          Left(s"player with such id already exists in room $name")
      }
}

final object Room {

}