package com.durakonline.game

final case class CardPair(
  attacker: Card,
  defender: Option[Card]
)