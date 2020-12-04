package com.durakonline.game

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._

class BoardSpec extends AnyFlatSpec {
  
  "Board.attack and Board.defend" should "work as expected" in {
    val attacker = Card(Value.Ten, Suit.Clubs)
    val weakDefender = Card(Value.Two, Suit.Clubs)
    val strongDefender = Card(Value.Ace, Suit.Clubs)
    val wrongSuitDefender = Card(Value.Ace, Suit.Diamonds)
    val trumpDefender = Card(Value.Two, Suit.Hearts, isTrump = true)

    val board1 = Board.empty.attack(attacker).get

    board1.pairsCount shouldBe 1
    board1.isThreatened shouldBe true

    board1.defend(weakDefender, attacker) match {
      case Some(value) => fail(s"unexpected some: $value")
      case None => succeed
    }

    board1.defend(strongDefender, attacker) match {
      case Some(value) => value.isThreatened shouldBe false
      case None => fail("unexpected None")
    }

    board1.defend(wrongSuitDefender, attacker) match {
      case Some(value) => fail(s"unexpected some: $value")
      case None => succeed
    }

    board1.defend(trumpDefender, attacker) match {
      case Some(value) => value.isThreatened shouldBe false
      case None => fail("unexpected None")
    }



    board1.attack(Card(Value.Ten, Suit.Diamonds)) match {
      case Some(value) => 
        value.pairsCount shouldBe 2
        value.takeCards.size shouldBe 2
        value.isThreatened shouldBe true
      case None => fail("unexpected None")
    }

    board1.attack(Card(Value.Five, Suit.Diamonds)) match {
      case Some(value) => fail(s"unexpected Some: $value")
      case None => succeed
    }
  }
}
