import React from 'react'
import { Link } from 'react-router-dom'
import styled, { css } from 'styled-components'

import {GameMode, RoomData} from '../../model/RoomData'

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

const RoomNameSpan = styled(OverflowingSpan)`
  text-align: left;
  padding-left: 1em;
  padding-right: 1em;
`

const GameModeSpan = styled(OverflowingSpan)`
  font-size: 0.8em;
  width: 5em;
  margin-left: auto;
  flex-shrink: 0;

  border-left: 1px solid #a7a7a7;
  border-right: 1px solid #a7a7a7;
`

const PlayerCountSpan = styled(OverflowingSpan)`
  font-size: 0.8em;
  width: 2em;
  margin-right: 0.5em;
  padding-left: 0.25em;
  flex-shrink: 0;
`

const StyledLink = styled(Link)`
  color: inherit;
  text-decoration: none;
`

function maxPlayersFromGameMode (mode: GameMode): number {
  switch (mode) {
    case "lobby": return NaN
    case "of24": return 2
    case "of36": return 4
    case "of52": return 6
  }
}

function gameModeToHumanReadable (mode: GameMode): string {
  switch (mode) {
    case "lobby": return "lobby"
    case "of24": return "24 cards"
    case "of36": return "36 cards"
    case "of52": return "52 cards"
  }
}

interface RoomRecordProps {
  room: RoomData
}

export default function RoomRecord (props: RoomRecordProps) {

  const maxPlayers: number = maxPlayersFromGameMode(props.room.mode)

  const isDisabled: boolean = props.room.playerCount === maxPlayers

  return (
    <StyledLink to={`room/${props.room.roomName}`}>
      <Container disabled={isDisabled}>
        <RoomNameSpan>{props.room.roomName}</RoomNameSpan>
        <GameModeSpan>{gameModeToHumanReadable(props.room.mode)}</GameModeSpan>
        <PlayerCountSpan>{props.room.playerCount}/{maxPlayers}</PlayerCountSpan>
      </Container>
    </StyledLink>
  )
}