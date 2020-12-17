import React, { useState } from 'react'
import styled from 'styled-components'
import RoomJoinPrompt from './RoomJoinPrompt';


export default function Room() {
  const [isJoined, setIsJoined] = useState(false)
  return (
    isJoined 
      ? <span>joined</span>
      : <RoomJoinPrompt onJoinedChange={joined => setIsJoined(joined)} />
  )
}
