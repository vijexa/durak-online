
import React, { useEffect, useState } from 'react'
import styled from 'styled-components'

import { CardData, CardPairData } from '../../model/CardData'
import Card from './Card'

const OuterContainer = styled.div`
  position: absolute;
  left: 50%;
`

const InnerContainer = styled.div`
  position: relative;
  left: -50%;
  height: 0;
  display: flex;
  flex-direction: row;

  > *:nth-child(n + 2) {
    margin-left: 0.25em;
  }
`

const PairContainer = styled.div`
  display: flex;
  flex-direction: column;

  > *:nth-child(n + 2) {
    margin-top: -1.5em;
  }
`

interface BoardProps {
  pairs: CardPairData[]

  className?: string
}

export default function Board ({pairs, className}: BoardProps) {

  return (
    <OuterContainer className={className}>
      <InnerContainer>
        {
          pairs.map(pair =>
            <PairContainer key={pair.attacker.value + pair.attacker.suit} >
              <Card cardData={pair.attacker} />
              {
                pair.defender
                  ? <Card cardData={pair.defender} />
                  : <div />
              }
            </PairContainer>
          )
        }
      </InnerContainer>
    </OuterContainer>
  )
}

