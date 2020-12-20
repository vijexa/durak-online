
import { Codec, GetType, number, string, array } from 'purify-ts/Codec'

import { CardDataCodec } from './CardData'

export const HandDataCodec = Codec.interface({
  cards: array(CardDataCodec)
})

export type HandData = GetType<typeof HandDataCodec>

export const SecretHandDataCodec = Codec.interface({
  size: number,
  playerName: string
})

export type SecretHandData = GetType<typeof SecretHandDataCodec>
