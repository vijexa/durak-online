package com.durakonline.game

import cats.effect.Sync

// TODO: the idea is to be able to easily create decks of different sizes
// and compositions, and also implement getting random card and reduce Deck

final case class Deck(cards: Set[Card]) extends CardContainer {
  def addCard(card: Card): Deck = 
    this.copy(cards = cards + card)
  
  protected def removeCard(card: Card): Deck = 
    this.copy(cards = cards - card)

  def drawCard[F[_] : Sync]: F[Option[(Card, Deck)]] = 
    implicitly[Sync[F]].delay(
      for {
        card <- cards.iterator.drop(
          util.Random.nextInt(cards.size)
        ).nextOption
      } yield (card, removeCard(card))
    )
}

object Deck  {

  private def generate (lowestCard: Int) = 
    Value.values.toSet
      .filter(_.value >= lowestCard)
      .flatMap(value =>
        Suit.values.toSet.map(suit =>
          Card(value, suit)
        )
      )

  // 9, 10, J, Q, K, A
  def of24 = Deck(
    generate(9)
  )

  // 6, 7, 8, 9, 10, J, Q, K, A
  def of36 = Deck(
    generate(6)
  )

  // 2, 3, 4, 5, 6, 7, 8, 9, 10, J, Q, K, A
  def of52 = Deck(
    generate(2)
  )

}
