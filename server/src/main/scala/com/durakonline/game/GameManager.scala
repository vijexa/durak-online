package com.durakonline.game

import com.durakonline.model._
import com.durakonline.game.network.Messages.Request._
import com.durakonline.game.network.Messages.Response._
import com.durakonline.game.TurnResolvement.AttackerResolvement._
import com.durakonline.game.TurnResolvement.DefenderResolvement._

import org.http4s.Response

import io.circe.parser.decode, io.circe.syntax._

import cats.effect._
import cats.syntax.all._
import cats.effect.concurrent.Ref

import fs2.{ Stream, Pipe }
import fs2.concurrent.Queue

import org.http4s.server.websocket._
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame._

import scala.concurrent.duration._


case class GameManager [F[_] : Concurrent] (
  lobby: Ref[F, Lobby[F]],
  gameState: Option[GameState],
  players: Vector[Player],
  playersReady: Vector[Player]
) {
  def getRoom (ref: Ref[F, GameManager[F]]) = 
    lobby.get.map(_.rooms.collectFirst{ 
      case (_, room) if room.gameManager == ref => room 
    })

  def addPlayer (player: Player): Either[String, GameManager[F]] =
    if (!players.contains(player)) copy(players = players :+ player).asRight
    else Left("player is already connected")

  def addToReady (playerId: UUIDString): Either[String, GameManager[F]] =
    for {
      player <- players.find(_.id == playerId).toRight("no player with specified id")
      _ <- Either.cond(
        !playersReady.exists(_.id == playerId),
        (),
        "player is already ready"
      )
    } yield copy(playersReady = playersReady :+ player)

  def removePlayer (player: Player): GameManager[F] =
    copy(
      players = players.filterNot(_ == player),
      playersReady = playersReady.filterNot(_ == player)
    )
}

object GameManager {
  def empty [F[_] : Concurrent] (lobby: Ref[F, Lobby[F]]): F[Ref[F, GameManager[F]]] =
    Ref.of[F, GameManager[F]](GameManager[F](lobby, None, Vector.empty, Vector.empty))

  def markReady [F[_]] (manager: Ref[F, GameManager[F]], id: UUIDString): F[Text] =
    manager.modify(m => 
      m.addToReady(id) match {
        case Left(error) => (m, Text(Error(error).asJson.noSpaces))
        case Right(newManager) => (newManager, Text(OK.apply.asJson.noSpaces))
      }
    )

  def startGame [F[_] : Concurrent] (
    manager: Ref[F, GameManager[F]], 
    id: UUIDString
  ) (implicit CF: ConcurrentEffect[F]): F[Text] = {
    for {
      roomOpt <- manager.get.map(_.getRoom(manager)).flatten
      resp <- manager.modify { m =>
        val gameStateOrError = for {
          _ <- m.playersReady.find(_.id == id).toRight("player is not ready")
          _ <- Either.cond(
            m.playersReady.length >= 2, 
            (), 
            "at least 2 people should be ready"
          )
          room <- roomOpt.toRight(
            "can't find room for this manager, data is unsynchronised"
          )
          _ <- Either.cond(
            room.owner == id,
            (),
            "only owner can start game"
          )
        } yield GameState.startGame[F](m.playersReady, room.mode)

        (
          m, 
          gameStateOrError match {
            case Left(error) => CF.delay(Text(Error(error).asJson.noSpaces))
            case Right(gameState) => 
              for {
                gameState <- gameState
                _ <- manager.update(_.copy(gameState = gameState)) 
              } yield Text(OK.apply.asJson.noSpaces)
          }
        )
      }.flatten
    } yield resp
  }

  def doGenericAction [F[_] : Concurrent] (
    manager: Ref[F, GameManager[F]],
    action: GameState => Either[String, GameState]
  ): F[Text] = {
    for {
      errorOrResponse <- manager.modify{ m =>
        val gameStateOrError = for {
          gameState <- m.gameState.toRight("game has not started yet")
          newState <- action(gameState)
        } yield newState

        (
          gameStateOrError match {
            case Left(error) => (m, Text(Error(error).asJson.noSpaces))
            case Right(gameState) => 
              val resolvements = gameState.resolveTurn
              val resolvedGameState = if (
                (
                  resolvements.contains(DefenderCannotDefend) &&
                  resolvements.contains(AttackerCanAttack)
                ) || (
                  resolvements.contains(DefenderCannotDefend) &&
                  resolvements.contains(AttackerCannotAttack) 
                ) || (
                  resolvements.contains(DefenderCanDefend) &&
                  resolvements.contains(AttackerCannotAttack) &&
                  !gameState.board.isThreatened
                )
              ) gameState.finishTurn(resolvements).getOrElse(gameState) else gameState

              (
                m.copy(gameState = resolvedGameState.some), 
                Text(OK.apply.asJson.noSpaces)
              )
          }
        )
      }
    } yield errorOrResponse
  }

  def attackPlayer [F[_] : Concurrent] (
    manager: Ref[F, GameManager[F]],
    attacker: Player,
    card: Card
  ): F[Text] =
    doGenericAction(
      manager, 
      _.attackPlayer(attacker, card).toRight("can't attack")
    )

  def defendPair [F[_] : Concurrent] (
    manager: Ref[F, GameManager[F]],
    defender: Player,
    card: Card,
    target: Card
  ): F[Text] = 
    doGenericAction(
      manager, 
      _.defendPair(defender, target, card)
        .toRight("can't defend")
    )

  // give player game state when it changes
  def createPlayerNotifier [F[_] : Concurrent : Timer] (
    manager: Ref[F, GameManager[F]],
    player: Player
  ): F[Stream[F, WebSocketFrame.Text]] = {
    for {
      gs <- manager.get.map(_.gameState)
      prevGameState <- Ref.of[F, Option[GameState]](gs)
    } yield Stream.repeatEval(
      for {
        m <- manager.get 
        resp <- prevGameState.modify(prevState => 
          m.gameState.collect {
            case state if state.some != prevState => 
              GameStateMessage.of(state, player).asJson.noSpaces
          } match {
            case Some(value) => (m.gameState, Some(value))
            case None => (prevState, None)
          }
        )
      } yield resp
    ).collect{ case Some(json) => Text(json) }.metered(100.millisecond) 
  }

  def createConnection [F[_] : Concurrent : Timer] (
    manager: Ref[F, GameManager[F]], 
    player: Player
  )(implicit CF: ConcurrentEffect[F]): F[Response[F]] = {

    val echoReply: Pipe[F, WebSocketFrame, WebSocketFrame] =
      _.evalMap{
        case Text(msg, _) => decode[Action](msg) match {
          case Left(error) => CF.delay(Text(Error(error.getMessage()).asJson.noSpaces))
          case Right(action) => action match {
            case MarkReady(_, playerId) => markReady(manager, playerId)
            case StartGame(_, playerId) => startGame(manager, playerId)
            case AttackPlayer(_, card) => attackPlayer(manager, player, card)
            case DefendPair(_, card, target) => 
              defendPair(manager, player, card, target)
            case TakeCards(_) => doGenericAction(
              manager,
              state => state.takeCards(player).toRight("cannot take cards")
            )
            case FinishAttack(_) => doGenericAction(
              manager,
              state => state.endAttackerTurn(player).toRight("cannot end turn")
            )
          }
        }

        case Close(data) => 
          manager.update(
            _.removePlayer(player).copy(gameState = None)
          ) as Text("bye")
        
        case _ => CF.delay(Text("unsupported"))
      }

    for {
      playerNotifier <- createPlayerNotifier(manager, player)
      resp <- Queue
        .unbounded[F, WebSocketFrame]
        .flatMap { q =>
          WebSocketBuilder[F].build(
            receive = q.enqueue,
            send = q.dequeue.through(echoReply) merge playerNotifier
          )
        }
    } yield resp
  }
}
