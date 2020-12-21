import React from 'react'
import styled, { css } from 'styled-components'
import { List } from 'purify-ts/List'

import sendJsonWebsocket from '../../util/sendJsonWebsocket'
import { AttackActionCodec, DefendAction, DefendActionCodec } from '../../model/Actions'
import { CardData } from '../../model/CardData'
import { BoardData } from '../../model/BoardData'
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

export type HighlightOptions = 'no' | 'attacker' | 'defender'

const ContainerInner = styled.div<{highlight: HighlightOptions}>`
  position: relative;
  right: -50%;
  display: flex;
  flex-direction: row;
        border-radius: 0.25em;
  ${({highlight}) => {
    if (highlight === 'attacker') {
      return css`
        border: dashed 0.25em yellow;
      `
    } else if (highlight === 'defender')
      return css`
        border: dashed 0.25em lightskyblue;
      `
    }
  }}

  > *:nth-child(n + 2) {
    margin-left: -0.75em;
  }
`

interface HandProps {
  cards?: CardData[]
  cardAmount: number
  isDefender?: boolean
  socket: WebSocket,
  boardData: BoardData
  highlight: HighlightOptions

  className?: string
}

export default function Hand ({
  cards, 
  cardAmount, 
  isDefender, 
  socket, 
  className,
  boardData,
  highlight
}: HandProps) {

  return (
    <ContainerOuter className={className}>
      <ContainerInner highlight={highlight}>
        {
          cards 
            ? cards.map(
              card => isDefender
                ? <StyledCard 
                  key={card.value + card.suit} 
                  cardData={card}
                  onClick={() =>
                    List.find(pair => pair.defender === null, boardData.pairs)
                      .ifJust(targetPair =>
                        sendJsonWebsocket<DefendAction>(
                          socket, 
                          {card: card, target: targetPair.attacker},
                          DefendActionCodec
                        )
                      )
                  }
                />
                : <StyledCard 
                  key={card.value + card.suit} 
                  cardData={card}
                  onClick={() =>
                    sendJsonWebsocket(
                      socket, 
                      {card: card},
                      AttackActionCodec
                    )
                  }
                />
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
