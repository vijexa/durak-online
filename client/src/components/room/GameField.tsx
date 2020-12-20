
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
`

interface GameFieldProps {
  gameState: GameState

  className?: string
}

export default function GameField ({gameState, className}: GameFieldProps) {

  return (
    <Container className={className}>
      <StyledDeck deckData={gameState.deck} />
      <HandField 
        players={gameState.players}
        yourHand={gameState.hand}
        yourIndex={gameState.yourIndex}
      />
    </Container>
  )
}

