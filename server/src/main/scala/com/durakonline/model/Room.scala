package com.durakonline.model

import com.durakonline.game.GameMode
import com.durakonline.game.GameManager

import cats.effect.concurrent.Ref

final case class Room [F[_]](
  name: RoomName, 
  password: RoomPassword,
  owner: UUIDString,
  players: Map[UUIDString, Player],
  mode: GameMode,
  gameManager: Ref[F, GameManager[F]]
) {
  /**
    * Returns room with added player, or an error description
    *
    * @param player
    * @return
    */
  def addPlayer (player: Player): Either[ErrorDescription, Room[F]] = 
    for {
      _ <- players.find{ case (_, currPlayer) => currPlayer.id == player.id }
        .fold[Either[String, Unit]](Right(()))(
          _ => Left(s"player with such id already exists in room $name")
        )
      _ <- Either.cond(
        players.size < mode.maxPlayers,
        (),
        "maximum amount of players in a room reached"
      )
    } yield copy(players = players + (player.id -> player))

  /**
    * Returns room with removed player, if any
    *
    * @param id
    * @return
    */
  def removePlayer (id: UUIDString): Room[F] = 
    this.copy(players = players.filter{ case (currId, _) => currId != id })
}

final object Room {

}