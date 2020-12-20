
import { Codec, GetType, nullable, boolean } from 'purify-ts/Codec'
import { List } from 'purify-ts/List'

export const values = ["2", "3", "4", "5", "6", "7", "8", "9", "T", "J", "Q", "K", "A"] as const
export type Value = typeof values[number] 

export const ValueCodec = Codec.custom<Value>({
  decode: str => List.find(x => x === str, [...values]).toEither("invalid value"),
  encode: str => str
})

export const suits = ["c", "d", "h", "s"] as const
export type Suit = typeof suits[number]

export const SuitCodec = Codec.custom<Suit>({
  decode: str => List.find(x => x === str, [...suits]).toEither("invalid suit"),
  encode: str => str
})

export const CardDataCodec = Codec.interface({
  suit: SuitCodec,
  value: ValueCodec,
  isTrump: boolean
})

export type CardData = GetType<typeof CardDataCodec>

export const CardPairDataCodec = Codec.interface({
  attacker: CardDataCodec,
  defender: nullable(CardDataCodec)
})

export type CardPairData = GetType<typeof CardPairDataCodec>
