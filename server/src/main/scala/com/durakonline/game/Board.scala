package com.durakonline.game

import cats.implicits._

final case class Board (pairs: Vector[CardPair]) {

  val pairsCount = pairs.size

  private def getAllCards: Vector[Card] =
    pairs.flatMap{
      case CardPair(attacker, defenderOpt) => Vector(attacker.some, defenderOpt)
    }
    .collect{ case Some(value) => value }

  // should allow to add new CardPair if board is empty or there is a Card with 
  // the same value
  def attack (card: Card): Option[Board] =
    if (
      (pairsCount == 0 && pairsCount <= 6) || 
      getAllCards.find(_.value == card.value).isDefined
    ) {
      this.copy(pairs = pairs :+ CardPair(card, None)).some
    } else None 

  def defend (card: Card, target: Card): Option[Board] = {
    val targetIndex = pairs.indexWhere(_.attacker == target)
    if (targetIndex >= 0) {
      val targetPair = pairs(targetIndex)

      def returnSome = this.copy(
        pairs = pairs.updated(
          targetIndex, targetPair.copy(defender = card.some)
        )
      ).some

      // should allow to defend pairs if card is with same suit and better value
      // or it is a trump
      if (targetPair.defender.isEmpty)
        if (card.isTrump && !targetPair.attacker.isTrump) returnSome
        else if (card.isTrump || targetPair.attacker.suit == card.suit)
          if (targetPair.attacker.value.num < card.value.num) returnSome
          else None
        else None
      else None

    } else None
  }

  def takeCards = getAllCards

  def isThreatened: Boolean = pairs.exists(_.defender.isEmpty)

  def isEmpty: Boolean = pairs.isEmpty

}

object Board {
  def empty = Board(Vector.empty)
}