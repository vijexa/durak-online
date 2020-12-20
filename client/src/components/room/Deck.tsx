import React from 'react'
import styled from 'styled-components'

import { CardData } from '../../model/CardData'
import { DeckData } from '../../model/DeckData'
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

const OuterContainer = styled.div`
  position: absolute;
  right: 50%;
  bottom: 50%;
`

const InnerContainer = styled.div`
  position: relative;
  right: -50%;
  bottom: -50%;
  width: 3em;
  height: 3em;
`

interface DeckProps {
  deckData: DeckData

  className?: string
}

export default function Deck ({deckData, className}: DeckProps) {

  if (deckData.cardCount > 0) return (
    <OuterContainer className={className}>
      <InnerContainer>
        <TrumpCard cardData={deckData.trumpCard} rotation={90} />

        {
          [...Array(deckData.cardCount - 1)].map((_, i) => 
            <StyledCard key={i} rotation={getRandomInt(-15, 15)} back />
          )
        }
      </InnerContainer>
    </OuterContainer>
  ) 
  else return <div />
}
