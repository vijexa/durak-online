import React, { useEffect, useState } from 'react'
import styled from 'styled-components'

import { RoomData, RoomDataListCodec } from '../../model/RoomData'
import RoomRecord from './RoomRecord'
import fetchJson from '../../util/fetchJsonGet'
import CreateRoomButton from './CreateRoomButton'

const Container = styled.div`
  display: flex;
  flex-direction: column;
  width: 90%;
`

interface RoomListProps {
  
}

export default function RoomList (props: RoomListProps) {
  const [rooms, setRooms] = useState<RoomData[]>([])

  const periodicFetching = () => {
    fetchJson("all-rooms", RoomDataListCodec).then(either => {
      console.log(either)
      setRooms(
        either.orDefault({ rooms: [] }).rooms
          .filter(room => room.roomName !== "lobby")
      )
    })
  }

  useEffect(() => {
    periodicFetching()
    const interval = setInterval(periodicFetching, 10000)

    return (() => clearInterval(interval))
  }, [])

  return (
    <Container>
      {
        rooms.map((room) =>
          <RoomRecord key={room.roomName} room={room}/>
        )
      }
      <CreateRoomButton />
    </Container>
  )
}