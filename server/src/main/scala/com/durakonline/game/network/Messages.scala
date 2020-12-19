package com.durakonline.game.network

import com.durakonline.model._
import com.durakonline.game._

import eu.timepit.refined.auto._

import io.circe.generic.JsonCodec
import io.circe.refined._

import cats.syntax.functor._

import io.circe._
import io.circe.generic.semiauto._

object Messages {

  object ImplicitCodecs {
    implicit val suitDecoder: Decoder[Suit] = Decoder.decodeString.emap(str =>
      Suit.values.find(_.representation == str).toRight("invalid suit")
    )

    implicit val suitEncoder: Encoder[Suit] = 
      Encoder.encodeString.contramap[Suit](_.representation)

    implicit val valueDecoder: Decoder[Value] = Decoder.decodeString.emap(str =>
      Value.values.find(_.representation == str).toRight("invalid card value")
    )

    implicit val valueEncoder: Encoder[Value] = 
      Encoder.encodeString.contramap[Value](_.representation)

    implicit val cardCodec: Codec[Card] = deriveCodec[Card]

    implicit val deckCodec: Codec[Deck] = deriveCodec[Deck]

    implicit val handCodec: Codec[Hand] = deriveCodec[Hand]

    implicit val cardPair: Codec[CardPair] = deriveCodec[CardPair]

    implicit val boardCodec: Codec[Board] = deriveCodec[Board]
  }

  object Request {
    import ImplicitCodecs._

    sealed trait Action {
      val action: String
    }

    @JsonCodec case class MarkReady (
      action: "mark-ready", 
      playerId: UUIDString
    ) extends Action

    @JsonCodec case class StartGame (
      action: "start-game",
      playerId: UUIDString
    ) extends Action

    @JsonCodec case class AttackPlayer (
      action: "attack-player",
      card: Card
    ) extends Action

    @JsonCodec case class DefendPair (
      action: "defend-pair",
      card: Card,
      target: Card
    ) extends Action

    implicit val decodeEvent: Decoder[Action] =
      List[Decoder[Action]](
        Decoder[MarkReady].widen,
        Decoder[StartGame].widen,
        Decoder[AttackPlayer].widen,
        Decoder[DefendPair].widen
      ).reduceLeft(_ or _)
  }

  object Response {
    import ImplicitCodecs._

    @JsonCodec case class OK private  (status: ResponseStatus)
    object OK {
      def apply = new OK("ok")
    }

    @JsonCodec case class Error private  (
      status: ResponseStatus, 
      errorDescription: ErrorDescription
    )
    object Error {
      def apply (errorDescription: String) = new Error("error", errorDescription)
    }

    @JsonCodec case class SecretDeck (
      topCard: Option[Card],
      trumpCard: Card
    )

    object SecretDeck {
      def apply (deck: Deck): SecretDeck = SecretDeck(
        deck.cards.headOption, 
        deck.trumpCard
      )
    }

    @JsonCodec case class SecretHand (
      size: Int,
      playerName: UserName
    )

    @JsonCodec case class GameStateMessage (
      hand: Hand,
      board: Board,
      deck: SecretDeck,
      players: Vector[SecretHand],
      discarded: Int,
      whoseTurn: Int
    )

    object GameStateMessage {
      def of (gameState: GameState, player: Player): Option[GameStateMessage] = {
        for {
          PlayerWithHand(player, hand) <- gameState.players.find(_.player == player)
        } yield GameStateMessage(
          hand = hand,
          board = gameState.board,
          deck = SecretDeck(gameState.deck),
          players = gameState.players.filterNot(_.player == player).map{
            case PlayerWithHand(player, hand) => SecretHand(hand.size, player.name)
          },
          discarded = gameState.discardPile.cards.size,
          whoseTurn = gameState.players.indexWhere(_.player == gameState.whoseTurn)
        )
      }
    }
  }
}
