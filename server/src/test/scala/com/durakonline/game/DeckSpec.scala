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
    val deck = Deck.of24
    testDeck(deck, 9, 24)
  }
  
  // 6, 7, 8, 9, 10, J, Q, K, A
  "Deck.of36" should "generate correct deck" in {
    val deck = Deck.of36
    testDeck(deck, 6, 36)
  }

  // 2, 3, 4, 5, 6, 7, 8, 9, 10, J, Q, K, A
  "Deck.of52" should "generate correct deck" in {
    val deck = Deck.of52
    testDeck(deck, 2, 52)
  }

  "Deck.drawCard" should "work correctly" in {
    val deck = Deck.of36

    {
      for {
        opt <- deck.drawCard[IO]
      } yield for {
        (card, newDeck) <- opt
        _ = println(card)
      } yield newDeck.cards + card shouldBe deck.cards
    }.unsafeRunSync
    
  }
}
