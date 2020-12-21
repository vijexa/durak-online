package com.durakonline.game

import com.durakonline.model.Player

import cats.implicits._
import cats.effect.Sync

final case class GameState (
  deck: Deck,
  board: Board,
  discardPile: DiscardPile,
  players: Vector[PlayerWithHand],
  whoseTurn: Player,
  attackerFinished: Boolean = false
) {

  def resolveTurn: Set[TurnResolvement] = {
    import com.durakonline.game.TurnResolvement._
    import AttackerResolvement._, DefenderResolvement._, OthersAttackResolvement._

    val (attacker, _) = getPlayerWithHand(whoseTurn).get
    
    val attackerResolvement = 
      if (attacker.hand.cards.exists(card => board.canAttack(card))) 
        AttackerCanAttack
      else 
        AttackerCannotAttack

    val defenderResolvement = 
      if (
        getDefender.hand.cards.exists(card => 
          board.pairs.exists(pair =>
             board.canDefend(card, pair)
          )
        )
      ) DefenderCanDefend
      else 
        DefenderCannotDefend

    val othersAttackResolvement =
        if (attackerFinished) 
          OthersCanAttack 
        else 
          OthersCannotAttack

    // TODO: resolve if game is finished
    
    Set(attackerResolvement, defenderResolvement, othersAttackResolvement)
  }

  def finishTurn: Option[GameState] = {
    val (newDefender, newDiscardPile) = 
      if (board.isThreatened) (
        getDefender.addCardsToHand(board.takeCards),
        discardPile
      ) else (
        getDefender,
        discardPile.addCards(board.takeCards)
      )

    val defenderIndex = players.indexWhere(_.player == newDefender.player)

    val playersWithUpdatedDefender = players.updated(defenderIndex, newDefender)

    val whoseTurn = 
      if (board.isThreatened) players(nextPlayerIndex(defenderIndex)).player
      else newDefender.player

    // TODO: take look at this with clear head and write tests

    for {
      (newDeck, newHands) <- deck.deal(playersWithUpdatedDefender.map(_.hand))
    } yield this.copy(
      deck = newDeck,
      board = Board.empty,
      discardPile = newDiscardPile,
      players = players zip newHands map { 
        case (pwh, newHand) => pwh.copy(hand = newHand) 
      },
      whoseTurn = whoseTurn,
      attackerFinished = false
    )
    
  }

  // allows attacker to finish their move to allow other players to
  // participate or for defender to take cards
  def endAttackerTurn (attacker: Player): Option[GameState] = 
    for {
      (attackerWithHand, index) <- getPlayerWithHand(attacker)
      if attacker == whoseTurn
    } yield this.copy(
      attackerFinished = true
    )

  // allows defender to take cards from board and basically give up his turn
  def takeCards (defender: Player): Option[GameState] = 
    for {
      (defenderWithHand, index) <- getPlayerWithHand(defender)

      if defender == getDefender.player

      if attackerFinished

      cardsFromBoard = board.takeCards

      defenderWithNewCards = defenderWithHand.addCardsToHand(cardsFromBoard)
    } yield this.copy(
      players = players.updated(index, defenderWithNewCards),
      board = Board.empty,
      attackerFinished = false,
      whoseTurn = players(nextPlayerIndex(index)).player
    )

  def attackPlayer (
    attacker: Player, 
    card: Card
  ): Option[GameState] = for {

    // confirm that such attacker exists and get it and it's index
    (attackerWithHand, attackerIndex) <- getPlayerWithHand(attacker)

    // check if this is an attackers turn or attacker did his move for other
    // players to be able to add cards
    if attacker == whoseTurn || attackerFinished

    // can't attack if there are more pairs than cards in a defender hand
    // or more than 6 pairs
    if board.pairsCount <= getDefender.hand.size && board.pairsCount <= 6
    
    // get attacker hand without this card, and also confirm that attacker 
    // actually has this card 
    newAttackerWithHand <- attackerWithHand removeCardFromHand card

    // try to attack; Board.attack handles everything else
    newBoard <- board attack card
  } yield this.copy(
    board = newBoard,
    players = players.updated(attackerIndex, newAttackerWithHand)
  )

  def defendPair (
    defender: Player,
    target: Card,
    card: Card
  ): Option[GameState] = for {

    // confirm that this defender exists and get it and it's index
    (defenderWithHand, defenderIndex) <- getPlayerWithHand(defender)

    // confirm that this is true defender 
    if (getDefender == defenderWithHand)

    // get defender hand without this card and check if it actually exists
    newDefenderWithHand <- defenderWithHand removeCardFromHand card

    // try to defend
    newBoard <- board.defend(card, target)

  } yield this.copy(
    board = newBoard,
    players = players.updated(defenderIndex, newDefenderWithHand)
  )

  private def getPlayerIndexWithStep (curr: Int, step: Int) =
    if (curr + step >= players.length) (players.length - curr) % step
    else curr + step

  protected def nextPlayerIndex (curr: Int): Int =
    getPlayerIndexWithStep(curr, 1)

  protected def playerAfterNextIndex (curr: Int): Int =
    getPlayerIndexWithStep(curr, 2)

  protected def getDefender: PlayerWithHand = 
    players(nextPlayerIndex(players.indexWhere(_.player == whoseTurn)))

  protected def getPlayerWithHand (
    player: Player
  ): Option[(PlayerWithHand, Int)] =
    players.zipWithIndex find {
      case (playerWH, index) => playerWH.player == player
    }
}

object GameState {

  /**
    * Returns initiated GameState. Internally generates shuffled Deck and deals
    * cards from it to the players
    *
    * @param players
    */
  def startGame[F[_] : Sync] (
    players: Vector[Player], 
    mode: GameMode
  ): F[Option[GameState]] = {
    for {
      // TODO: fix this 
      deck <- mode match {
        case GameMode.DeckOf24 => Deck.of24[F]
        case GameMode.DeckOf36 => Deck.of36[F]
        case GameMode.DeckOf52 => Deck.of52[F]
      }
      
      emptyHands = players.map(_ => Hand.empty)

    } yield for {
      (dealtDeck, dealtHands) <- deck.deal(emptyHands)

      if players.size <= 6

      playersWithHands = players zip dealtHands map { 
        case (player, hand) => PlayerWithHand(player, hand) 
      }
    } yield GameState(
      deck = dealtDeck, 
      board = Board.empty, 
      discardPile = DiscardPile.empty, 
      players = playersWithHands,
      whoseTurn = players.head
    )
  }

}
