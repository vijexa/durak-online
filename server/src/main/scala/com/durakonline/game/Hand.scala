package com.durakonline.game

final case class Hand(cards: Set[Card]) extends CardContainer {
  def addCard (card: Card): Hand = 
    this.copy(cards = cards + card)
  
  protected def removeCard (card: Card): Hand = 
    this.copy(cards = cards - card)

  def takeCard (card: Card): Option[(Card, Hand)] =
    for {
      _ <- cards.find(_ == card)
    } yield (card, removeCard(card))
  
}
