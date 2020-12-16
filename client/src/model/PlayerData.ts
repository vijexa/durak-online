
import { Codec, GetType, string, array } from 'purify-ts/Codec'

export const PlayerDataCodec = Codec.interface({
  name: string
})

export type PlayerData = GetType<typeof PlayerDataCodec>

export const PlayerDataListCodec = Codec.interface({
  players: array(PlayerDataCodec)
})

export type PlayerDataList = GetType<typeof PlayerDataListCodec>

export const PlayerDataWithIdCodec = Codec.interface({
  name: string,
  id: string
})

export type PlayerDataWithId = GetType<typeof PlayerDataWithIdCodec>

export const PlayerDataWithIdListCodec = Codec.interface({
  players: array(PlayerDataWithIdCodec)
})

export type PlayerDataWithIdList = GetType<typeof PlayerDataWithIdListCodec>
