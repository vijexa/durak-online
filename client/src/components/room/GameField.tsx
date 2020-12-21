
import React, { useEffect, useState } from 'react'
import styled from 'styled-components'

import { CardData, CardPairData } from '../../model/CardData'
import { GameState } from '../../model/GameStateData'
import Board from './Board'
import Deck from './Deck'
import HandField from './HandField'

const Container = styled.div`
  position: relative;
  top: 0%;
  width: 100vw;
  height: 90vh;
`

const StyledDeck = styled(Deck)`
  bottom: 50vh;
`

const StyledBoard = styled(Board)`
  bottom: 40vh;
`

function getNextIndex (currI: number, length: number): number {
  if (currI + 1 !== length) return currI + 1
  else return 0
}

interface GameFieldProps {
  gameState: GameState
  socket: WebSocket

  className?: string
}

export default function GameField ({gameState, socket, className}: GameFieldProps) {

  const isDefender = getNextIndex(
    gameState.whoseTurn, 
    gameState.players.length
  ) === gameState.yourIndex

  return (
    <Container className={className}>
      <StyledDeck deckData={gameState.deck} />
      <StyledBoard pairs={gameState.board.pairs} />
      <HandField 
        players={gameState.players}
        yourHand={gameState.hand}
        yourIndex={gameState.yourIndex}
        isDefender={isDefender}
        socket={socket}
        boardData={gameState.board}
      />
    </Container>
  )
}

