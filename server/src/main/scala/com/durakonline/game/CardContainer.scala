package com.durakonline.game

trait CardContainer {
  val cards: Set[Card]

  def addCard (card: Card): CardContainer

  def removeCard (card: Card): CardContainer
}
