package com.durakonline.game

final case class DiscardPile (cards: Vector[Card]) extends CardContainer {
  def addCard(card: Card): DiscardPile = 
    this.copy(cards = cards :+ card)

  def addCards(newCards: Iterable[Card]): DiscardPile =
    this.copy(cards = cards ++ newCards)
  
  protected def removeCard(card: Card): DiscardPile = ???
  
}

object DiscardPile {
  def empty: DiscardPile = DiscardPile(Vector.empty)
}