package com.durakonline.game

final case class Card(value: Value, suit: Suit, isTrump: Boolean = false)

object Card {
  // TODO: some parsing-verification stuff
}