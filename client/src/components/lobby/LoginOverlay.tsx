import React, { useState } from 'react'
import styled from 'styled-components'
import Popup from 'reactjs-popup'
import { useCookies } from 'react-cookie'

import fetchJsonPost from '../../util/fetchJsonPost'
import { Status, StatusCodec } from '../../model/Status'
import { PlayerData } from '../../model/PlayerData'
import PopupContainer from './PopupContainer'

interface LoginOverlayProps {

}

export default function LoginOverlay (props: LoginOverlayProps) {
  const [{id}] = useCookies(['id'])

  const [enabled, setEnabled] = useState(id === undefined)

  const [name, setName] = useState('')

  const createNewPlayer = (name: string) =>
    fetchJsonPost<Status, PlayerData>('new-player', { name: name }, StatusCodec)
      .then(result => {
        console.log("resss", result)
        result.orDefault({status: 'error'}).status === 'ok'
          ? setEnabled(false)
          : setEnabled(true)
      })

  return (
    <Popup 
      open={enabled} 
      overlayStyle={{background: 'rgba(100, 100, 100, 0.5)'}} 
      closeOnDocumentClick={false}
      modal
    >
      <PopupContainer>
        <span>Enter your username to start:</span>
        <input 
          type="text" 
          onChange={e => setName(e.target.value)} 
          placeholder="Username"
        />
        <button onClick={() => {createNewPlayer(name)}}>Login</button>
      </PopupContainer>
    </Popup>
  )
}