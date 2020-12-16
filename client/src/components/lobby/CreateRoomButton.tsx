import React, { useState } from 'react'
import styled from 'styled-components'
import Popup from 'reactjs-popup'

import PopupContainer from './PopupContainer'
import { GameMode, RoomCreationData } from '../../model/RoomData'
import fetchJsonPost from '../../util/fetchJsonPost'
import { Status, StatusCodec } from '../../model/Status'

const Trigger = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 2em;
  margin-top: 0.5em;
  border-radius: 1em;
  
  background-color: white;
  cursor: pointer;
  
  transition: background 0.5s;
  :hover {
    color: #000000;
    background-color: #eaff8f;
  }
`

interface CreateRoomButtonProps {
  
}

export default function CreateRoomButton (props: CreateRoomButtonProps) {

  const [isOpen, setIsOpen] = useState(false)
  const [name, setName] = useState('')
  const [password, setPassword] = useState('')
  const [gameMode, setGameMode] = useState<GameMode>('of24')

  const createRoom = () => {
    const body = {
      name: name,
      password: password,
      mode: gameMode
    }

    fetchJsonPost<Status, RoomCreationData>(
      'create-room',
      body,
      StatusCodec
    ).then(resp =>{
      console.log(resp)
      resp.orDefault({status: 'error'}).status === 'ok' 
        ? setIsOpen(false)
        : setIsOpen(true)
    })
  }

  return (
    <Trigger onClick={_ => setIsOpen(true)} >+ Create new room
      <Popup 
        open={isOpen}
        overlayStyle={{background: 'rgba(100, 100, 100, 0.5)'}}
        modal
      >
        <PopupContainer>
          <span>Enter room name</span>
          <input 
            type="text" 
            placeholder="Room name"
            onChange={e => setName(e.target.value)}
          />
          <span>Enter password for your room (or leave empty)</span>
          <input 
            type="password" 
            placeholder="Room password" 
            onChange={e => setPassword(e.target.value)}
          />
          <span>Select game mode</span>
          <select onChange={e => setGameMode(e.target.value as GameMode)}>
            <option value="of24">24 cards</option>
            <option value="of36">36 cards</option>
            <option value="of52">52 cards</option>
          </select>
          <button onClick={_ => {createRoom()}}>
            Create room
          </button>
        </PopupContainer>
      </Popup>
    </Trigger>
  )
}