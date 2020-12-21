import React, { useEffect, useState } from 'react'
import styled from 'styled-components'
import { useCookies } from 'react-cookie'

import { DefendActionCodec, MarkReadyActionCodec, StartGameActionCodec } from '../../model/Actions'
import sendJsonWebsocket from '../../util/sendJsonWebsocket'
import Board from './Board'
import Card from './Card'
import Deck from './Deck'
import Hand from './Hand'
import GameField from './GameField'
import { GameState, GameStateDataCodec } from '../../model/GameStateData'

const Container = styled.div`
  position: absolute;
  top: 0%;
  left: 0%;
`

const UIContainer = styled.div`
  position: fixed;
  bottom: 0;
  left: 0;
  font-size: 1.5em;
  text-align: center;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: row;
  width: 100%;
  height: 10%;
  border-radius: 0.5em 0.5em 0 0;

  padding: 1em 1em 1em 1em;
  color: black;
  background-color: white;

  > *:nth-child(n + 2) {
    margin-left: 1em;
  }
`

const StyledButton = styled.button`
  background-color: #247443;
  color: #ffffff;
  outline: none;
  border: solid 3px black;
  font-size: 0.75em;
  padding: 0.2em;
  border-radius: 0.2em;

  :active {
    transform: translateY(5px);
  }
`

const markReady = (socket: WebSocket, id: string) => sendJsonWebsocket(
  socket, 
  { playerId: id }, 
  MarkReadyActionCodec
)

const startGame = (socket: WebSocket, id: string) => sendJsonWebsocket(
  socket, 
  { playerId: id }, 
  StartGameActionCodec
)

interface GameProps {
  roomName: string
}

export default function Game ({roomName}: GameProps) {
  const [{id}] = useCookies(['id'])

  const [socket, setSocket] = useState<WebSocket | undefined>(undefined)

  const [gameState, setGameState] = useState<GameState | undefined>(undefined)

  useEffect(() => {
    const socket = new WebSocket('ws://localhost:8080/room-connect/' + roomName)

    socket.addEventListener('message', (event) => {
      const parsed = GameStateDataCodec.decode(JSON.parse(event.data))
      console.log(parsed)
      parsed.ifRight(gameState => setGameState(gameState))
    })

    setSocket(socket)

    return () => {
      socket.close()
      setSocket(undefined)
    }
  }, [])

  if (socket) return (
    <Container>
      { gameState ? <GameField gameState={gameState} socket={socket} /> : <div /> }
      <UIContainer>
        <StyledButton onClick={() => markReady(socket, id)}>Mark ready</StyledButton>
        <StyledButton onClick={() => startGame(socket, id)}>Start game</StyledButton>
      </UIContainer>
    </Container>
  )
  else return (
    <div>loading...</div>
  )
}