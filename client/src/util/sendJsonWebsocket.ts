import { Codec } from "purify-ts";

export default function sendJsonWebsocket <T> (
  socket: WebSocket, 
  data: T, 
  codec: Codec<T>
): void {
  socket.send(
    JSON.stringify(
      codec.encode(data)
    ) as string
  )
}