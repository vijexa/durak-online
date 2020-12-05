package com.durakonline.game

import com.durakonline.model.Player

import cats.implicits._
import cats.effect.Sync

final case class GameState private (
  deck: Deck,
  board: Board,
  discardPile: DiscardPile,
  players: Vector[PlayerWithHand],
  whoseTurn: Player,
  attackerFinished: Boolean = false
) {
  
  import PlayerWithHandImplicits._

  def attackPlayer (
    attacker: Player, 
    defender: Player, 
    card: Card
  ): Option[GameState] = for {

    // confirm that such attacker exists and get it and it's index
    (attackerWithHand, attackerIndex) <- players.zipWithIndex find {
      case (playerWH, index) => playerWH.player == attacker
    }

    // check if attacker did his move for other players to be able to add cards
    if attackerFinished

    defenderWithHand <- players find defender.==

    // can't attack if there are more pairs than cards in a defender hand
    // or more than 6 pairs
    if board.pairsCount <= defenderWithHand.size && board.pairsCount <= 6
    
    // get attacker hand without this card, and also confirm that attacker 
    // actually has this card 
    newAttackerHand <- attackerWithHand takeCard card

    // try to attack; Board.attack handles everything else
    newBoard <- board attack card

    newAttackerWithHand = attackerWithHand.copy(hand = newAttackerHand)
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
    (defenderWithHand, defenderIndex) <- players.zipWithIndex find {
      case (playerWH, index) => playerWH.player == defender
    }

    // get defender hand without this card and check if it actually exists
    newDefenderHand <- defenderWithHand takeCard card
    newDefenderWithHand = defenderWithHand.copy(hand = newDefenderHand)

    // try to defend
    newBoard <- board.defend(card, target)

  } yield this.copy(
    board = newBoard,
    players = players.updated(defenderIndex, newDefenderWithHand)
  )

  protected def nextPlayerIndex (prev: Int): Int =
    if (prev < players.length - 1) prev + 1
    else 0

  protected def getDefender: Player = 
    players(nextPlayerIndex(players.indexOf(whoseTurn)))
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
      deck <- mode match {
        case DeckOf24 => Deck.of24[F]
        case DeckOf36 => Deck.of36[F]
        case DeckOf52 => Deck.of52[F]
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