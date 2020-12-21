package com.durakonline.game

sealed trait GameMode {
  val maxPlayers: Int
}

object GameMode {
  case object LobbyMode extends GameMode {
    val maxPlayers: Int = Int.MaxValue
  }

  case object DeckOf24 extends GameMode {
    val maxPlayers: Int = 2
  }

  case object DeckOf36 extends GameMode {
    val maxPlayers: Int = 4
  }

  case object DeckOf52 extends GameMode {
    val maxPlayers: Int = 6
  }
}
