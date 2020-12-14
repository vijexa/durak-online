package com.durakonline.game

sealed trait GameMode 

object GameMode {
  case object LobbyMode extends GameMode
  case object DeckOf24 extends GameMode
  case object DeckOf36 extends GameMode
  case object DeckOf52 extends GameMode
}
