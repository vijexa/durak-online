package com.durakonline.game

final case class Hand(cards: Set[Card]) extends CardContainer {
  def size: Int = cards.size

  def addCard (card: Card): Hand = 
    this.copy(cards = cards + card)
  
  def addCards (newCards: Seq[Card]): Hand = 
    this.copy(cards = cards ++ newCards)
  
  protected def removeCard (card: Card): Hand = 
    this.copy(cards = cards - card)

  def takeCard (card: Card): Option[Hand] =
    for {
      _ <- cards.find(_ == card)
    } yield removeCard(card)

  def hasCard (card: Card) = cards.exists(_ == card)
  
}

object Hand {
  def empty: Hand = Hand(Set.empty)
}
