
import React, { useEffect, useState } from 'react'
import styled from 'styled-components'

import { CardData, CardPairData } from '../../model/CardData'
import Card from './Card'

const Container = styled.div`
  position: absolute;
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
    <Container className={className}>
      {
        pairs.map(pair =>
          <PairContainer>
            <Card cardData={pair.attacker} />
            {
              pair.defender
                ? <Card cardData={pair.defender} />
                : <div />
            }
          </PairContainer>
        )
      }
    </Container>
  )
}

