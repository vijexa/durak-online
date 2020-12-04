package com.durakonline.game

import cats.implicits._
import cats.effect.Sync

import util.Random

// TODO: the idea is to be able to easily create decks of different sizes
// and compositions, and also implement getting random card and reduce Deck

final case class Deck(cards: Vector[Card]) extends CardContainer {
  private val cardsNLimit = 6

  val size = cards.length

  def deal (hands: Vector[Hand]): Option[(Deck, Vector[Hand])] = {
    if (hands.size > 0) {
      if (size > 0) {
        hands.foldLeft((Vector.empty[Hand], cards)) {
          case ((hands, cards), hand) => 
            val (taken, rest) = cards.splitAt(cardsNLimit - hand.size)
            (hands :+ hand.addCards(taken), rest)
        } match {
          case (hands, cards) => (this.copy(cards = cards), hands).some
        }
      } else None
    } else None
  }

  def addCard(card: Card): Deck = 
    this.copy(cards = cards :+ card)
  
  protected def removeCard(card: Card): Deck = 
    this.copy(cards = cards filterNot card.==)

  protected def removeTopMostCard: Deck =
    this.copy(cards = cards.tail)

  def drawCard[F[_] : Sync]: F[Option[(Card, Deck)]] = 
    implicitly[Sync[F]].delay(
      for {
        card <- cards.headOption
      } yield (card, this.removeTopMostCard)
    )
}

object Deck  {

  protected def generate[F[_] : Sync] (lowestCard: Int): F[Vector[Card]] = 
    implicitly[Sync[F]].delay(
      Random.shuffle(
        Value.values
          .filter(_.value >= lowestCard)
          .flatMap(value =>
            Suit.values.map(suit =>
              Card(value, suit)
            )
          ).toVector
      )
    )

  // 9, 10, J, Q, K, A
  def of24[F[_] : Sync]: F[Deck] =
    generate(9) map Deck.apply

  // 6, 7, 8, 9, 10, J, Q, K, A
  def of36[F[_] : Sync]: F[Deck] =
    generate(6) map Deck.apply

  // 2, 3, 4, 5, 6, 7, 8, 9, 10, J, Q, K, A
  def of52[F[_] : Sync]: F[Deck] = 
    generate(2) map Deck.apply

}
