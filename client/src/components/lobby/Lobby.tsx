import React from 'react'
import styled from 'styled-components'
import RoomList from './RoomList'

import sampleRoomData from '../../sample-data/sampleRoomData'

const Container = styled.div`
  display: flex;
  min-width: 50%;
  max-width: 90%;
  color: #1d1d1d;
`

interface LobbyProps {

}

export default function Lobby (props: LobbyProps) {
  return (
    <Container>
      <RoomList rooms={sampleRoomData} />
    </Container>
  )
}