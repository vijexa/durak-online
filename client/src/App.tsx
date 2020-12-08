import React from 'react';
import styled from 'styled-components'
import Lobby from './components/lobby/Lobby';

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
    <StyledApp>
      <Lobby />
    </StyledApp>
  );
}

export default App;
