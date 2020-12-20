import React, { useEffect, useState } from 'react'
import styled from 'styled-components'

import fetchJsonPost from '../../util/fetchJsonPost'
import { Status, StatusCodec } from '../../model/Status'
import { RoomNameWithPassword } from '../../model/RoomData'

const Container = styled.div`
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

interface RoomJoinPromptProps {
  roomName: string
  onJoinedChange: (isJoined: boolean) => void
}

export default function RoomJoinPrompt ({
  roomName, 
  onJoinedChange
}: RoomJoinPromptProps) {
  const [password, setPassword] = useState('')

  const joinRoom = () => {
    fetchJsonPost<Status, RoomNameWithPassword>(
      'join-room', 
      {
        roomName: roomName,
        roomPassword: password
      },
      StatusCodec
    ).then(respE => {
      console.log(respE)
      respE.ifRight(resp =>
        resp.status === 'ok' ? onJoinedChange(true) : onJoinedChange(false)
      )
    })
  }

  return (
    <Container>
        <span>Enter "{roomName}" room password:</span>
        <input 
          type='text'
          placeholder='password' 
          onChange={e => setPassword(e.target.value)}
        />
        <button onClick={_ => joinRoom()}>Join</button>
    </Container>
  )
}