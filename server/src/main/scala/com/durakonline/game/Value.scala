package com.durakonline.game

import enumeratum._

sealed trait Value extends EnumEntry {
  val value: Int
}

object Value extends Enum[Value] {
  val values = findValues

  case object Two   extends Value { val value = 2 } 
  case object Three extends Value { val value = 3 } 
  case object Four  extends Value { val value = 4 } 
  case object Five  extends Value { val value = 5 } 
  case object Six   extends Value { val value = 6 } 
  case object Seven extends Value { val value = 7 } 
  case object Eight extends Value { val value = 8 } 
  case object Nine  extends Value { val value = 9 } 
  case object Ten   extends Value { val value = 10 } 
  case object Jack  extends Value { val value = 11 } 
  case object Queen extends Value { val value = 12 } 
  case object King  extends Value { val value = 13 } 
  case object Ace   extends Value { val value = 14 } 
}
