import React, { useEffect, useState } from 'react'
import styled from 'styled-components'

import Card from './Card'
import Deck from './Deck'
import Hand from './Hand'

const UIContainer = styled.div`
  position: fixed;
  bottom: 0;
  left: 0;
  font-size: 1.5em;
  text-align: center;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  width: 100%;
  border-radius: 0.5em;

  padding: 0 1em 1em 1em;
  color: black;
  background-color: white;

  > * {
    margin-top: 1em;
  }
`

const StyledHand = styled(Hand)`
  top: 15%;
`

const StyledHand2 = styled(Hand)`
  top: 5%;
`

interface GameProps {
  roomName: string
}

export default function Game ({roomName}: GameProps) {

  return (
    <div>
      <Deck trumpCard={{suit: 'c', value: 'T'}} cardAmount={52} />
      <StyledHand 
        cards={[{suit: 'c', value: 'T'}, {suit: 'd', value: 'Q'}]} 
        cardAmount={2}
      />
      <StyledHand2 
        cardAmount={12}
      />
      <UIContainer>
        UI and stuff
      </UIContainer>
    </div>
  )
}