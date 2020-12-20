
import { Codec, GetType, array } from 'purify-ts/Codec'

import { CardPairDataCodec } from './CardData'

export const BoardDataCodec = Codec.interface({
  pairs: array(CardPairDataCodec)
})

export type BoardData = GetType<typeof BoardDataCodec>
