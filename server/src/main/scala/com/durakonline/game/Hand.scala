package com.durakonline.game

final case class Hand(cards: Set[Card]) extends CardContainer {
  def addCard(card: Card): CardContainer = ???
  
  def removeCard(card: Card): CardContainer = ???
  
}
