import React from 'react'
import styled from 'styled-components'

const Container = styled.div`
  font-size: 1.5em;
  text-align: center;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 1.75em;
  border-radius: 0.5em;

  background-color: white;
`

interface LobbyHeaderProps {

}

export default function LobbyHeader (props: LobbyHeaderProps) {
  return (
    <Container>
      Durak playing rooms
    </Container>
  )
}