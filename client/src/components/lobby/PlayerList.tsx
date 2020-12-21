import React, { useEffect, useState } from 'react'
import styled from 'styled-components'

import fetchJson from '../../util/fetchJsonGet'
import { PlayerData, PlayerDataListCodec } from '../../model/PlayerData'

const Container = styled.div`
  font-size: 1.5em;
  text-align: center;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: fit-content;
  border-radius: 0.5em;

  background-color: white;
`

const HeaderSpan = styled.span`
  padding: 0 0.5em 0 0.5em;
`

const PlayerSpan = styled.span`
  padding: 0.1em 0 0.1em 0;
  font-size: 0.8em;
`

interface PlayerListProps {

}

export default function PlayerList (props: PlayerListProps) {

  const [players, setPlayers] = useState<PlayerData[]>([])

  const periodicFetching = () => {
    fetchJson("get-all-player-names", PlayerDataListCodec).then(either => {
      console.log(either)
      setPlayers(
        either.orDefault({ players: [] }).players
      )
    })
  }

  useEffect(() => {
    periodicFetching()
    const interval = setInterval(periodicFetching, 2000)

    return (() => clearInterval(interval))
  }, [])

  return (
    <Container>
      <HeaderSpan>Players:</HeaderSpan>
      {
        players.map((player, i) =>
          <PlayerSpan key={player.name + i} >
            {player.name}
          </PlayerSpan>  
        )
      }
    </Container>
  )
}