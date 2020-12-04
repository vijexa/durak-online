package com.durakonline.game

import cats.implicits._
import cats.effect.Sync

import util.Random

// TODO: the idea is to be able to easily create decks of different sizes
// and compositions, and also implement getting random card and reduce Deck

final case class Deck(cards: Vector[Card], trumpCard: Card) extends CardContainer {
  private val cardsNLimit = 6

  val size = cards.length

  def deal (hands: Vector[Hand]): Option[(Deck, Vector[Hand])] = {
    if (hands.size > 0) {
      if (size > 0) {
        // add cards to each hand so that amount of cards was 6;
        // does not add cards when deck is empty or amount of cards
        // in deck is >= 6
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

  protected def removeTopmostCard: Deck =
    this.copy(cards = cards.tail)

  def drawCard[F[_] : Sync]: F[Option[(Card, Deck)]] = 
    implicitly[Sync[F]].delay(
      for {
        card <- cards.headOption
      } yield (card, this.removeTopmostCard)
    )
}

object Deck  {

  protected def generate[F[_] : Sync] (lowestCard: Int): F[(Vector[Card], Card)] = 
    implicitly[Sync[F]].delay{
      val shuffled = Random.shuffle(
        Value.values
          .filter(_.num >= lowestCard)
          .flatMap(value =>
            Suit.values.map(suit =>
              Card(value, suit)
            )
          ).toVector
      )

      val trump = shuffled.last

      val shuffledWithTrumps = shuffled.map(card => 
        if (card.suit == trump.suit) card.copy(isTrump = true) else card
      )

      (shuffledWithTrumps, trump)
    }

  private def makeDeck(tuple: (Vector[Card], Card)) = 
    tuple match { case (cards, trump) => Deck(cards, trump) }

  // 9, 10, J, Q, K, A
  def of24[F[_] : Sync]: F[Deck] =
    generate(9) map makeDeck

  // 6, 7, 8, 9, 10, J, Q, K, A
  def of36[F[_] : Sync]: F[Deck] =
    generate(6) map makeDeck

  // 2, 3, 4, 5, 6, 7, 8, 9, 10, J, Q, K, A
  def of52[F[_] : Sync]: F[Deck] = 
    generate(2) map makeDeck

}
