
import { Codec } from 'purify-ts/Codec'
import { Either, Left } from 'purify-ts/Either'

export default function fetchJsonPost <RES, REQ> (
  uri: string,
  requestBody: REQ,
  responseCodec: Codec<RES>
): Promise<Either<string, RES>> {
  return (
    fetch(
      '/' + uri,
      {
        method: 'POST',
        credentials: 'include',
        body: JSON.stringify(requestBody)
      }
    ).then(response =>
      response.json()
        .then(json =>
          responseCodec.decode(json)
        )
        .catch(error =>
          Left(error)
        )
    )
  )
}
