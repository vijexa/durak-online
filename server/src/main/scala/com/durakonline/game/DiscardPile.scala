package com.durakonline.game

final case class DiscardPile (cards: Vector[Card]) extends CardContainer {
  def addCard(card: Card): CardContainer = 
    this.copy(cards = cards :+ card)

  def addCards(newCards: Iterable[Card]): CardContainer =
    this.copy(cards = cards ++ newCards)
  
  protected def removeCard(card: Card): CardContainer = ???
  
}

object DiscardPile {
  def empty:DiscardPile = DiscardPile(Vector.empty)
}