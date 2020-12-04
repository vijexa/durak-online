package com.durakonline.game

import enumeratum._

sealed trait Value extends EnumEntry {
  val num: Int
}

object Value extends Enum[Value] {
  val values = findValues

  case object Two   extends Value { val num = 2 } 
  case object Three extends Value { val num = 3 } 
  case object Four  extends Value { val num = 4 } 
  case object Five  extends Value { val num = 5 } 
  case object Six   extends Value { val num = 6 } 
  case object Seven extends Value { val num = 7 } 
  case object Eight extends Value { val num = 8 } 
  case object Nine  extends Value { val num = 9 } 
  case object Ten   extends Value { val num = 10 } 
  case object Jack  extends Value { val num = 11 } 
  case object Queen extends Value { val num = 12 } 
  case object King  extends Value { val num = 13 } 
  case object Ace   extends Value { val num = 14 } 
}
