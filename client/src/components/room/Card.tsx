import React, { useEffect, useState } from 'react'
import styled from 'styled-components'
import CardPicture from "@heruka_urgyen/react-playing-cards/lib/TcN"

import { CardData } from '../../model/CardData'

const CardContainer = styled.div<{rotation: number}>`
  transform: rotate(${({rotation}) => rotation}deg);
  height: 3em;

  > img {
    height: 100%;
  }
`

interface CardProps {
  cardData?: CardData
  rotation?: number
  back?: boolean

  className?: string
}

export default function Card ({cardData, back, rotation, className}: CardProps) {

  return (
    <CardContainer className={className} rotation={rotation ?? 0}>
      {
        cardData && !back
          ? <CardPicture card={cardData.value + cardData.suit} />
          : <CardPicture back />
      }
    </CardContainer>
  )
}
