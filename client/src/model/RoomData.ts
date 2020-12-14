
import { Codec, GetType, string, number, array } from 'purify-ts/Codec'
import { Left, Right } from 'purify-ts/Either'

export type GameMode = "lobby" | "of24" | "of36" | "of52"

export const GameModeCodec = Codec.custom<GameMode>({
  decode: input => (
    (input === "lobby" || input === "of24" || input === "of36" || input === "of52")
      ? Right(input)
      : Left(`failed to decode GameMode ${input}`)
  ),
  encode: input => input
})

export const RoomDataCodec = Codec.interface({
  roomName: string,
  mode: GameModeCodec,
  playerCount: number
})

export type RoomData = GetType<typeof RoomDataCodec>

export const RoomDataListCodec = Codec.interface({
  rooms: array(RoomDataCodec)
})

export type RoomDataList = GetType<typeof RoomDataListCodec>