
import React, { useEffect, useState } from 'react'
import styled from 'styled-components'

import { CardData, CardPairData } from '../../model/CardData'
import { HandData, SecretHandData } from '../../model/HandData'
import { BoardData } from '../../model/BoardData'
import Card from './Card'
import Board from './Board'
import Deck from './Deck'
import Hand from './Hand'

const Container = styled.div`
  position: relative;
  top: 0%;
  width: 100%;
  height: 100%;
`

const HandContainer = styled.div<{rotation: number}>`
  position: absolute;
  transform-origin: top center;
  transform: rotate(${({rotation}) => rotation}deg);
  top: 45vh;
  width: 100%;
  height: 40vmin;

  > * {
    position: absolute;
    bottom: 0%;
  }
`

interface HandFieldProps {
  players: SecretHandData[]
  yourHand: HandData
  yourIndex: number
  socket: WebSocket
  isDefender?: boolean
  boardData: BoardData

  className?: string
}

export default function HandField ({
  players, 
  yourHand, 
  yourIndex, 
  className,
  socket,
  isDefender,
  boardData
}: HandFieldProps) {

  const unorderedHands = players.map((player, i) =>
    i == yourIndex
      ? <Hand 
        cardAmount={yourHand.cards.length} 
        cards={yourHand.cards}
        isDefender={isDefender}
        socket={socket}
        boardData={boardData}
      />
      : <Hand cardAmount={player.size} socket={socket} boardData={boardData} />
  )

  const orderedHands = [
    ...unorderedHands.slice(yourIndex, unorderedHands.length),
    ...unorderedHands.slice(0, yourIndex)
  ]

  return (
    <Container className={className}>
      {
        orderedHands.map((hand, i) =>
          <HandContainer key={i} rotation={(360 / orderedHands.length) * i}>
            {hand}
          </HandContainer>
        )
      }
    </Container>
  )
}

