package com.durakonline.game

final case class Hand(cards: Set[Card]) extends CardContainer {
  def size: Int = cards.size

  def addCard (card: Card): Hand = 
    this.copy(cards = cards + card)
  
  def addCards (newCards: Seq[Card]): Hand = 
    this.copy(cards = cards ++ newCards)
  
  protected def removeCard (card: Card): Hand = 
    this.copy(cards = cards - card)

  def takeCard (card: Card): Option[(Card, Hand)] =
    for {
      _ <- cards.find(_ == card)
    } yield (card, removeCard(card))
  
}
