import React, { useState } from 'react'
import styled from 'styled-components'
import { useParams } from 'react-router-dom'

import RoomJoinPrompt from './RoomJoinPrompt'
import Game from './Game'


export default function Room() {
  const { roomName } = useParams<{roomName: string}>()
  const [isJoined, setIsJoined] = useState(false)

  return (
    isJoined 
      ? <Game roomName={roomName} />
      : <RoomJoinPrompt 
          roomName={roomName}
          onJoinedChange={joined => setIsJoined(joined)} 
        />
  )
}
