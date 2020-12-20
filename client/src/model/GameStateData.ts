import { Codec, GetType, array, number } from 'purify-ts/Codec'
import { BoardDataCodec } from './BoardData'

import { DeckDataCodec } from './DeckData'
import { HandDataCodec, SecretHandDataCodec } from './HandData'

export const GameStateDataCodec = Codec.interface({
  hand: HandDataCodec,
  board: BoardDataCodec,
  deck: DeckDataCodec,
  players: array(SecretHandDataCodec),
  discarded: number,
  whoseTurn: number
})

export type GameState = GetType<typeof GameStateDataCodec>
