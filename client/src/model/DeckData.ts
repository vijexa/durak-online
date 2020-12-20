
import { Codec, GetType, number, nullable } from 'purify-ts/Codec'

import { CardDataCodec } from './CardData'

export const DeckDataCodec = Codec.interface({
  topCard: nullable(CardDataCodec),
  trumpCard: CardDataCodec,
  cardCount: number
})

export type DeckData = GetType<typeof DeckDataCodec>
