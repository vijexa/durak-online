package com.durakonline.game

import enumeratum._

sealed trait Value extends EnumEntry {
  val num: Int
  val representation: String
}

object Value extends Enum[Value] {
  val values = findValues

  case object Two extends Value { 
    val num = 2 
    val representation = "2"
  } 
  
  case object Three extends Value { 
    val num = 3 
    val representation = "3"
  } 

  case object Four extends Value { 
    val num = 4 
    val representation = "4"
  } 
  
  case object Five extends Value { 
    val num = 5 
    val representation = "5"
  } 
  
  case object Six extends Value { 
    val num = 6 
    val representation = "6"
  } 
  
  case object Seven extends Value { 
    val num = 7 
    val representation = "7"
  } 
  
  case object Eight extends Value { 
    val num = 8 
    val representation = "8"
  } 
  
  case object Nine extends Value { 
    val num = 9 
    val representation = "9"
  } 
  
  case object Ten extends Value { 
    val num = 10 
    val representation = "T"
  } 
  
  case object Jack extends Value { 
    val num = 11 
    val representation = "J"
  } 
  
  case object Queen extends Value { 
    val num = 12 
    val representation = "Q"
  } 
  
  case object King extends Value { 
    val num = 13 
    val representation = "K"
  }   

  case object Ace extends Value { 
    val num = 14 
    val representation = "A"
  } 
}
