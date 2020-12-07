package com.durakonline.game

sealed trait TurnResolvement

object TurnResolvement {
  sealed trait AttackerResolvement extends TurnResolvement
  object AttackerResolvement {
    case object AttackerCanAttack extends AttackerResolvement
    case object AttackerCannotAttack extends AttackerResolvement
  }
  
  sealed trait DefenderResolvement extends TurnResolvement
  object DefenderResolvement {
    case object DefenderCanDefend extends DefenderResolvement
    case object DefenderCannotDefend extends DefenderResolvement
  }
  
  sealed trait OthersAttackResolvement extends TurnResolvement
  object OthersAttackResolvement {
    case object OthersCanAttack extends OthersAttackResolvement
    case object OthersCannotAttack extends OthersAttackResolvement
  }
  
  case object GameIsFinished extends TurnResolvement
}
