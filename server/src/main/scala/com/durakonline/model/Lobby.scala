package com.durakonline.model

final case class Lobby private (
  rooms: Map[RoomName, Room]
)

object Lobby {


}