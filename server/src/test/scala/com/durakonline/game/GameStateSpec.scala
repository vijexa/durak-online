package com.durakonline.game

import com.durakonline.model.Player

import eu.timepit.refined.auto._

import cats.effect.IO

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._

class GameStateSpec extends AnyFlatSpec {
  
  val players = Vector(
    Player("d09207f3-bf59-410e-8ab1-9b07d7189639", "foobar"),
    Player("6d8120a2-6435-41e1-9f3b-cb37dee46d65", "asdf")
  )

  "GameState.startGame" should "work as expected" in {
    val state1 = GameState.startGame[IO](
      players, 
      GameMode.DeckOf24
    ).unsafeRunSync().get

    state1.players.size shouldBe 2
    state1.deck.size shouldBe 12
    state1.whoseTurn shouldBe players.head

    GameState.startGame[IO](
      Vector.empty,
      GameMode.DeckOf24
    ).unsafeRunSync().isEmpty shouldBe true
  }

  val initial = GameState.apply(
    deck = Deck(Vector.empty, Card(Value.Two, Suit.Clubs, true)),
    board = Board.empty,
    discardPile = DiscardPile.empty,
    players = Vector(
      PlayerWithHand(
        players.head, 
        Hand(
          Set(
            Card(Value.Six, Suit.Hearts),
            Card(Value.Six, Suit.Diamonds),
            Card(Value.Ten, Suit.Clubs, true)
          )
        )
      ),
      PlayerWithHand(
        players.last,
        Hand(
          Set(
            Card(Value.Seven, Suit.Hearts),
            Card(Value.Ace, Suit.Diamonds)
          )
        )
      )
    ),
    whoseTurn = players.head,
    attackerFinished = false
  )

  def makeAttack = initial.attackPlayer( 
    players.head,
    Card(Value.Six, Suit.Hearts)
  ).get

  "GameState.attackPlayer" should "work as expected" in {
    // defending player tries to attack, but it's not his turn
    initial.attackPlayer(
      players.last, 
      Card(Value.Seven, Suit.Hearts)
    ).isEmpty shouldBe true

    // attacker doesn't have this card
    initial.attackPlayer( 
      players.head,
      Card(Value.Two, Suit.Clubs, true)
    ).isEmpty shouldBe true

    // this attack should be fine
    val attacked = makeAttack

    attacked.board.pairsCount shouldBe 1
    attacked.board.pairs.head.defender.isEmpty shouldBe true
    attacked.board.isThreatened shouldBe true

    val multipleAttacks = attacked.attackPlayer(
      players.head,
      Card(Value.Six, Suit.Diamonds)
    ).get

    multipleAttacks.board.pairsCount shouldBe 2
    multipleAttacks.board.isThreatened shouldBe true
  }

  def makeDefend = 
    makeAttack.defendPair(
      players.last, 
      Card(Value.Six, Suit.Hearts), 
      Card(Value.Seven, Suit.Hearts)
    ).get

  "GameState.defendPair" should "work as expected" in {
    val attacked = makeAttack

    // defender doesn't have this card
    attacked.defendPair(
      players.last, 
      Card(Value.Six, Suit.Hearts), 
      Card(Value.Eight, Suit.Hearts)
    ).isEmpty shouldBe true

    // there is no such card on board
    attacked.defendPair(
      players.last, 
      Card(Value.Five, Suit.Hearts), 
      Card(Value.Seven, Suit.Hearts)
    ).isEmpty shouldBe true
    
    // wrong suit
    attacked.defendPair(
      players.last, 
      Card(Value.Six, Suit.Hearts), 
      Card(Value.Ace, Suit.Diamonds)
    ).isEmpty shouldBe true

    val defended = makeDefend

    defended.board.isThreatened shouldBe false
  }

  "GameState.finishTurn" should "work as expected" in {
    val attacked = makeAttack

    // wrong player tries to finish turn
    attacked.endAttackerTurn(players.last).isEmpty shouldBe true

    attacked.endAttackerTurn(players.head).get.attackerFinished shouldBe true
  }

  "GameState.takeCards" should "work as expected" in {
    val attacked = makeAttack

    // tries to take cards on attacker turn
    attacked.takeCards(players.head).isEmpty shouldBe true

    val finished = attacked.endAttackerTurn(players.head).get

    // wrong player tries to take cards
    finished.takeCards(players.head).isEmpty shouldBe true

    val taken = finished.takeCards(players.last).get

    taken.whoseTurn shouldBe players.head
    taken.players.head.hand.size shouldBe 2
    taken.players.last.hand.size shouldBe 3
  }

  "GameState.resolveTurn" should "work as expected" in {
    import com.durakonline.game.TurnResolvement._
    import AttackerResolvement._, DefenderResolvement._, OthersAttackResolvement._

    makeAttack.resolveTurn shouldBe Set(AttackerCanAttack, DefenderCanDefend, OthersCanAttack)
    makeDefend.resolveTurn shouldBe Set(AttackerCanAttack, DefenderCanDefend, OthersCanAttack)

    val attackedTwice = makeAttack.attackPlayer(
      players.head, 
      Card(Value.Six, Suit.Diamonds)
    ).get

    attackedTwice.resolveTurn shouldBe Set(AttackerCannotAttack, DefenderCanDefend, OthersCanAttack)
    attackedTwice.endAttackerTurn(players.head).get
      .resolveTurn shouldBe Set(AttackerCannotAttack, DefenderCanDefend, OthersCannotAttack)

    initial.attackPlayer(
      players.head, 
      Card(Value.Ten, Suit.Clubs, true)
    ).get.resolveTurn shouldBe Set(AttackerCannotAttack, DefenderCannotDefend, OthersCanAttack)
  }


}
