import React from 'react'
import styled from 'styled-components'

import RoomData from '../../model/RoomData'

const Container = styled.div`
  display: flex;
  justify-content: space-between;
  width: 100%;
  height: 2em;

  * {
    text-align: left;
    width: 33%
  }
`

interface RoomRecordProps {
  room: RoomData
}

export default function RoomRecord (props: RoomRecordProps) {
  return (
    <Container>
      <div>{props.room.name}</div>
      <div>{props.room.mode}</div>
      <div>{props.room.playerCount}/{props.room.maxPlayers}</div>
    </Container>
  )
}