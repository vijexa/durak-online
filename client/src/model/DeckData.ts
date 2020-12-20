
import { Codec, GetType, number } from 'purify-ts/Codec'

import { CardDataCodec } from './CardData'

export const DeckDataCodec = Codec.interface({
  trumpCard: CardDataCodec,
  cardCount: number
})

export type DeckData = GetType<typeof DeckDataCodec>
