package com.durakonline.game

import enumeratum._

sealed trait Suit extends EnumEntry {
  val representation: String
}

object Suit extends Enum[Suit] {
  val values = findValues

  // this one ---> ♠
  case object Spades extends Suit {
    val representation = "s"
  }
  // this one ---> ♣
  case object Clubs extends Suit {
    val representation = "c"
  }
  // this one ---> ♥
  case object Hearts extends Suit {
    val representation = "h"
  }
  // this one ---> ♦
  case object Diamonds extends Suit {
    val representation = "d"
  }
}
