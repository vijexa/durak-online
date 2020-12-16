import React, { useState } from 'react'
import styled from 'styled-components'
import Popup from 'reactjs-popup'
import { useCookies } from 'react-cookie'

import fetchJsonPost from '../../util/fetchJsonPost'
import { Status, StatusCodec } from '../../model/Status'
import { PlayerData } from '../../model/PlayerData'

const Container = styled.div`
  font-size: 1.5em;
  text-align: center;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  width: 100%;
  border-radius: 0.5em;

  padding: 0 1em 1em 1em;
  background-color: white;

  > * {
    margin-top: 1em;
  }
`

interface LoginOverlayProps {

}

export default function LoginOverlay (props: LoginOverlayProps) {
  const [{id}] = useCookies(['id'])

  const [enabled, setEnabled] = useState(id === undefined)

  const [name, setName] = useState('')

  const newPlayer = (name: string) =>
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
      <Container>
        <span>Enter your username to start:</span>
        <input type="text" onChange={e => setName(e.target.value)}/>
        <button onClick={() => {newPlayer(name)}}>Login</button>
      </Container>
    </Popup>
  )
}