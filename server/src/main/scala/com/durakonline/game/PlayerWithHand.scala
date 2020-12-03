package com.durakonline.game

import com.durakonline.model.Player

final case class PlayerWithHand(
  player: Player,
  hand: Hand
)