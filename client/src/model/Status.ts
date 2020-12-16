
import { Codec, GetType, string, array } from 'purify-ts/Codec'
import { Left, Right } from 'purify-ts/Either'

export type StatusCode = 'ok' | 'error'

const StatusCodeCodec = Codec.custom<StatusCode>({
  decode: input => input === 'ok' || input === 'error' 
    ? Right(input) 
    : Left(`wrong status code ${input}`),
  encode: input => input
})

export const StatusCodec = Codec.interface({
  status: StatusCodeCodec
})

export type Status = GetType<typeof StatusCodec>
