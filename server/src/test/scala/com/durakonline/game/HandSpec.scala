package com.durakonline.game

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._

class HandSpec extends AnyFlatSpec {
  
  "Hand" should "work as expected" in {
    val card1 = Card(Value.Ace, Suit.Clubs)
    val card2 = Card(Value.Ten, Suit.Diamonds)

    val hand = Hand(Set(card1, card2))

    hand.takeCard(card1).map{
      case (_, hand2) => hand2 shouldBe Hand(Set(card2)) 
    }
    
    hand.takeCard(card1).map{ 
      case (_, hand2) => hand2.addCard(card1) shouldBe hand 
    }
  }
}
