package com.durakonline.game

trait CardContainer {
  val cards: Iterable[Card]

  def addCard (card: Card): CardContainer

  protected def removeCard (card: Card): CardContainer
}
