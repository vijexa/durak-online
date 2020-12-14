import React, { EffectCallback, useEffect, useState } from 'react'
import styled from 'styled-components'
import { Codec, GetType, string, number } from 'purify-ts/Codec'

import { RoomData, RoomDataListCodec, RoomDataList } from '../../model/RoomData'
import RoomRecord from './RoomRecord'
import { Either, Left } from 'purify-ts/Either'

const Container = styled.div`
  display: flex;
  flex-direction: column;
  width: 90%;
`

function fetchRooms (): Promise<Either<string, RoomDataList>> {
  return (
    fetch("http://localhost:8010/proxy/all-rooms")
      .then(response =>
        response.json()
          .then(json =>
            RoomDataListCodec.decode(json)
          )
          .catch(error =>
            Left(error)
          )
      )
  )
}

interface RoomListProps {
  rooms: RoomData[]
}

export default function RoomList (props: RoomListProps) {
  const [rooms, setRooms] = useState<RoomData[]>([])

  const periodicFetching = () => {
    fetchRooms().then(either => {
      console.log(either)
      setRooms(either.orDefault({ rooms: [] }).rooms
        .filter(room => room.roomName !== "lobby"))
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
    </Container>
  )
}