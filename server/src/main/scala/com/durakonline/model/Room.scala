package com.durakonline.model

final case class Room (
  name: RoomName, 
  password: RoomPassword, 
  players: Map[UUIDString, Player]
) {
  def addPlayer (player: Player): Room = 
    this.copy(players = players + (player.id -> player))
}

final object Room {

}