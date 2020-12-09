import React from 'react'
import styled from 'styled-components'

import RoomData from '../../model/RoomData'
import RoomRecord from './RoomRecord'

const Container = styled.div`
  display: flex;
  flex-direction: column;
  width: 90%;
`

interface RoomListProps {
  rooms: RoomData[]
}

export default function RoomList (props: RoomListProps) {
  return (
    <Container>
      {
        props.rooms.map((room) =>
          <RoomRecord key={room.name} room={room}/>
        )
      }
    </Container>
  )
}