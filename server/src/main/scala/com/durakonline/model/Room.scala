package com.durakonline.model

import eu.timepit.refined.auto._

final case class Room (
  name: RoomName, 
  password: RoomPassword, 
  players: Map[UUIDString, Player]
)

final object Room {

}