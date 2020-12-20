import React from 'react'
import styled from 'styled-components'

import { CardData } from '../../model/CardData'
import Card from './Card'

const StyledCard = styled(Card)`
  box-shadow: 0px 0px 3px 0px rgba(0,0,0,0.75);
`

const TrumpCard = styled(Card)`
  position: absolute;
  left: 1.2em;
`

const ContainerOuter = styled.div`
  position: absolute;
  right: 50%;
`

const ContainerInner = styled.div`
  position: relative;
  right: -50%;
  display: flex;
  flex-direction: row;

  > *:nth-child(n + 2) {
    margin-left: -0.75em;
  }
`

interface HandProps {
  cards?: CardData[]
  cardAmount: number

  className?: string
}

export default function Hand ({cards, cardAmount, className}: HandProps) {

  return (
    <ContainerOuter className={className}>
      <ContainerInner>
        {
          cards 
            ? cards.map(
              card => <StyledCard key={card.value + card.suit} cardData={card}/>
            ) 
            : cardAmount > 0
              ? [...Array(cardAmount)].map((_, i) => 
              <StyledCard key={i} back />
              )
              : <div />
        }
      </ContainerInner>
    </ContainerOuter>
  )
}
