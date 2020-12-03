package com.durakonline.game

trait CardContainer {
  val cards: Set[Card]

  def addCard (card: Card): CardContainer

  protected def removeCard (card: Card): CardContainer
}
