package com.durakonline.game

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import cats.effect.IO

class DockSpec extends AnyFlatSpec {
  
  def testDeck(deck: Deck, lowest: Int, size: Int) = {
    deck.cards.size shouldBe size
    deck.cards.count(_.value.value >= lowest) shouldBe size
    
    Suit.values.map(suit =>
      deck.cards.count(_.suit == suit) shouldBe (size / 4)
    )
  }

  // 9, 10, J, Q, K, A
  "Deck.of24" should "generate correct deck" in {
    val deck = Deck.of24[IO].unsafeRunSync()
    testDeck(deck, 9, 24)
  }
  
  // 6, 7, 8, 9, 10, J, Q, K, A
  "Deck.of36" should "generate correct deck" in {
    val deck = Deck.of36[IO].unsafeRunSync()
    testDeck(deck, 6, 36)
  }

  // 2, 3, 4, 5, 6, 7, 8, 9, 10, J, Q, K, A
  "Deck.of52" should "generate correct deck" in {
    val deck = Deck.of52[IO].unsafeRunSync()
    testDeck(deck, 2, 52)
  }

  "Deck.drawCard" should "work correctly" in {
    val deck = Deck.of36[IO].unsafeRunSync()

    {
      for {
        opt <- deck.drawCard[IO]
      } yield for {
        (card, newDeck) <- opt
        _ = println(card)
      } yield card +: newDeck.cards shouldBe deck.cards
    }.unsafeRunSync
    
  }

  "Deck.deal" should "deal cards correctly" in {
    val deck = Deck.of24[IO].unsafeRunSync()
    // cards that aren't used in deck above
    val spareCards = Deck.of52[IO].unsafeRunSync().cards.filter(_.value.value < 9)

    val emptyHand = Hand(Set.empty)

    val (dealt1, hands1) = deck.deal(Vector(emptyHand, emptyHand, emptyHand)).get

    hands1.foldLeft(0)((sum, hand) => sum + hand.size) shouldBe 18
    dealt1.size shouldBe 6

    // first hand has 1 card and should get 5, second has 3 cards and will get
    // only one because that's what left in the deck
    val (dealt2, hands2) = dealt1.deal(
      Vector(
        Hand(spareCards.take(1).toSet),
        Hand(spareCards.drop(1).take(3).toSet)
      )
    ).get

    println(hands2)

    hands2.foldLeft(0)((sum, hand) => sum + hand.size) shouldBe 10
    dealt2.size shouldBe 0

  }
}
