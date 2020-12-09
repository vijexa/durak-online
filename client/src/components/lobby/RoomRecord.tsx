import React from 'react'
import styled, { css } from 'styled-components'

import RoomData from '../../model/RoomData'

interface ContainerProps {
  disabled: boolean
}

const Container = styled.div<ContainerProps>`
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  height: 2em;
  margin-top: 0.5em;
  border-radius: 1em;

  ${({disabled}) => 
    disabled 
      ? css`
        background-color: #929292;
        color: #414141;
      `
      : css`
        background-color: white;
        cursor: pointer;
        
        transition: background 0.5s;
        :hover {
          color: #000000;
          background-color: #eaff8f;
        }
      `
  }
`

const OverflowingSpan = styled.span`
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
`

const RoomName = styled(OverflowingSpan)`
  text-align: left;
  padding-left: 1em;
  padding-right: 1em;

  flex-grow: 1;
`

const GameMode = styled(OverflowingSpan)`
  font-size: 0.8em;
  width: 5em;
  margin-left: auto;

  border-left: 1px solid #a7a7a7;
  border-right: 1px solid #a7a7a7;
`

const PlayerCount = styled(OverflowingSpan)`
  font-size: 0.8em;
  width: 2em;
  margin-right: 0.5em;
  padding-left: 0.25em;
`

interface RoomRecordProps {
  room: RoomData
}

export default function RoomRecord (props: RoomRecordProps) {

  const isDisabled = props.room.playerCount === props.room.maxPlayers

  return (
    <Container disabled={isDisabled}>
      <RoomName>{props.room.name}</RoomName>
      <GameMode>{props.room.mode}</GameMode>
      <PlayerCount>{props.room.playerCount}/{props.room.maxPlayers}</PlayerCount>
    </Container>
  )
}