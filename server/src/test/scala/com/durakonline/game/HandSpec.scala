package com.durakonline.game

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._

class HandSpec extends AnyFlatSpec {
  
  "Hand" should "work as expected" in {
    val card1 = Card(Value.Ace, Suit.Clubs)
    val card2 = Card(Value.Ten, Suit.Diamonds)

    val hand = Hand(Set(card1, card2))

    hand.takeCard(card1).map( _ shouldBe Hand(Set(card2)) )
    
    hand.takeCard(card1).map( _.addCard(card1) shouldBe hand )
  }
}
