import React from 'react'
import styled from 'styled-components'

import { CardData } from '../../model/CardData'
import Card from './Card'

function getRandomInt(min: number, max: number) {
  return Math.floor(Math.random() * Math.floor(Math.abs(max) + Math.abs(min))) + min
}

const StyledCard = styled(Card)`
  position: absolute;
  box-shadow: 0px 0px 3px 0px rgba(0,0,0,0.75);
`

const TrumpCard = styled(Card)`
  position: absolute;
  left: 1.2em;
`

const Container = styled.div`
  transform: rotate(0deg);
  position: absolute;
`

interface DeckProps {
  trumpCard: CardData
  cardAmount: number

  className?: string
}

export default function Deck ({trumpCard, cardAmount, className}: DeckProps) {

  if (cardAmount > 0) return (
    <Container className={className}>
      <TrumpCard cardData={trumpCard} rotation={90} />

      {
        [...Array(cardAmount - 1)].map((_, i) => 
          <StyledCard key={i} rotation={getRandomInt(-15, 15)} back />
        )
      }
    </Container>
  ) 
  else return <div />
}