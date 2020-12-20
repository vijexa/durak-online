
import { Codec } from 'purify-ts/Codec'

import { CardData } from './CardData'
import { Left } from 'purify-ts'

export type MarkReadyAction = {
  playerId: string
}

export const MarkReadyActionCodec = Codec.custom<MarkReadyAction>({
  decode: _ => Left("not implemented"),
  encode: action => ({ action: 'mark-ready', playerId: action.playerId })
})

export type StartGameAction = {
  playerId: string
}

export const StartGameActionCodec = Codec.custom<StartGameAction>({
  decode: _ => Left("not implemented"),
  encode: action => ({ action: 'start-game', playerId: action.playerId })
})

export type AttackAction = {
  card: CardData
}

export const AttackActionCodec = Codec.custom<AttackAction>({
  decode: _ => Left("not implemented"),
  encode: action => ({ action: 'attack-player', card: action.card })
})

export type DefendAction = {
  card: CardData,
  target: CardData
}

export const DefendActionCodec = Codec.custom<DefendAction>({
  decode: _ => Left("not implemented"),
  encode: action => ({ action: 'defend-pair', card: action.card, target: action.target })
})
