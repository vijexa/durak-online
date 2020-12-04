package com.durakonline.game

import cats.implicits._

final case class Board (pairs: Vector[CardPair]) {

  private def getAllCards: Vector[Card] =
    pairs.flatMap{
      case CardPair(attacker, defenderOpt) => Vector(attacker.some, defenderOpt)
    }
    .collect{ case Some(value) => value }

  // should allow to add new CardPair if board is empty or there is a Card with 
  // the same value
  def attack (card: Card): Option[Board] =
    if (
      pairs.size == 0 || 
      getAllCards.find(_.value == card.value).isDefined
    ) {
      this.copy(pairs = pairs :+ CardPair(card, None)).some
    } else None 

  // should allow to defend pairs if card is with same suit and better value
  // or it is a trump
  def defend (card: Card, target: Card): Option[Board] = {
    val targetIndex = pairs.indexWhere(_.attacker == target)
    if (targetIndex > 0) {
      val targetPair = pairs(targetIndex)

      if (
        targetPair.defender.isEmpty &&
        (targetPair.attacker.suit == card.suit || card.isTrump) &&
        targetPair.attacker.value.num < card.value.num
      ) {
        this.copy(
          pairs = pairs.updated(
            targetIndex, targetPair.copy(defender = card.some)
          )
        ).some
      } else None
    } else None
  }

}
