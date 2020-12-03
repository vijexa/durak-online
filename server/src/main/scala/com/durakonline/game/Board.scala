package com.durakonline.game

import cats.implicits._

final case class Board (pairs: Vector[CardPair]) {

  private def getAllCards: Vector[Card] =
    pairs.flatMap{
      case CardPair(attacker, defenderOpt) => Vector(attacker.some, defenderOpt)
    }
    .collect{ case Some(value) => value }

  // should allow to add new CardPair if board is empty or there is a Card with 
  // same value
  def attack (card: Card): Option[Board] =
    if (pairs.size == 0 || getAllCards.find(_.suit == card.suit).isDefined)
      this.copy(pairs = pairs :+ CardPair(card, None)).some
    else None 

  // should allow to defend pairs ugh i want to sleep idk 😴😴😴
  def defend (card: Card): Option[Board] = ???

}


   /*  for {
      lastUndefended <- pairs.find(_.defender.isEmpty)
    } yield this.copy(pairs = pairs :+ CardPair(card, None)).some
    if (pairs.last.defender.isDefined)
      
    else None */