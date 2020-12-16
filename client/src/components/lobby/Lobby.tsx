import React from 'react'
import styled from 'styled-components'

import RoomList from './RoomList'
import LobbyHeader from './LobbyHeader'
import PlayerList from './PlayerList'
import LoginOverlay from './LoginOverlay'

const ContainerVertical = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #1d1d1d;
  flex-grow: 1;

  margin-right: 1em;
  margin-bottom: 1em;
`

const ContainerHorizontal = styled.div`
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
  flex-wrap: wrap;
  min-width: 50%;
  max-width: 90%;
  color: #1d1d1d;
`

interface LobbyProps {

}

export default function Lobby (props: LobbyProps) {
  return (
    <ContainerHorizontal>
      <ContainerVertical>
        <LobbyHeader />
        <RoomList />
      </ContainerVertical>
      <PlayerList />
      <LoginOverlay />
    </ContainerHorizontal>
  )
}