package com.durakonline.game

import com.durakonline.model.Player

final case class PlayerWithHand(
  player: Player,
  hand: Hand
)

object PlayerWithHandImplicits {
  implicit def implicitPlayerWithHandToHand(pwh: PlayerWithHand) = pwh.hand
  implicit def implicitPlayerWithHandToPlayer(pwh: PlayerWithHand) = pwh.player
}