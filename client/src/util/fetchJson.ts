
import { Codec } from 'purify-ts/Codec'
import { Either, Left } from 'purify-ts/Either'

export default function fetchRooms <T> (
  uri: string,
  codec: Codec<T>
): Promise<Either<string, T>> {
  return (
    fetch("http://localhost:8010/proxy/" + uri)
      .then(response =>
        response.json()
          .then(json =>
            codec.decode(json)
          )
          .catch(error =>
            Left(error)
          )
      )
  )
}
