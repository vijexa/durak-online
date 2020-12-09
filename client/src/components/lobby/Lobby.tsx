import React from 'react'
import styled from 'styled-components'

import RoomList from './RoomList'
import LobbyHeader from './LobbyHeader'

import sampleRoomData from '../../sample-data/sampleRoomData'

const Container = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 50%;
  max-width: 90%;
  color: #1d1d1d;
`

interface LobbyProps {

}

export default function Lobby (props: LobbyProps) {
  return (
    <Container>
      <LobbyHeader />
      <RoomList rooms={sampleRoomData} />
    </Container>
  )
}