import React from 'react'
import styled from 'styled-components'
import { BrowserRouter as Router, Switch, Route } from 'react-router-dom'

import Lobby from './components/lobby/Lobby'
import Room from './components/room/Room'
import LoginOverlay from './components/lobby/LoginOverlay'

import backgroundTexture from './pool-table.png'


const StyledApp = styled.div`
  text-align: center;
  background-color: #015e24;
  background-image: url(${backgroundTexture});
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  font-size: calc(10px + 2vmin);
  color: white;
`

function App() {
  return (
    <Router>
      <StyledApp>
        <LoginOverlay />

        <Switch>
          <Route path="/room/:roomName">
            <Room />
          </Route>
          <Route path="/">
            <Lobby />
          </Route>
        </Switch>
      </StyledApp>
    </Router>
  );
}

export default App;
