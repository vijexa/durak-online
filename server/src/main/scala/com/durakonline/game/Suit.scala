package com.durakonline.game

import enumeratum._

sealed trait Suit extends EnumEntry

object Suit extends Enum[Suit] {
  val values = findValues

  // this one ---> ♠
  case object Spades extends Suit
  // this one ---> ♣
  case object Clubs extends Suit
  // this one ---> ♥
  case object Hearts extends Suit
  // this one ---> ♦
  case object Diamonds extends Suit
}
