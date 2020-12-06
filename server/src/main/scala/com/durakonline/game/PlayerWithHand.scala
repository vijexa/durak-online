package com.durakonline.game

import com.durakonline.model.Player

final case class PlayerWithHand(
  player: Player,
  hand: Hand
) {
  def removeCardFromHand(card: Card): Option[PlayerWithHand] = 
    hand.takeCard(card).map(hand => this.copy(hand = hand))
}
