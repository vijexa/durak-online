package com.durakonline.game

sealed trait GameMode 

case object DeckOf24 extends GameMode
case object DeckOf36 extends GameMode
case object DeckOf52 extends GameMode