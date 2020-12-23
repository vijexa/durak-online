# Durak Online

My course project for [Evolution Scala bootcamp](https://github.com/evolution-gaming/scala-bootcamp). 

Server in Scala, frontend is in Typescript with React.

## Running

Start react dev server:

```shell
cd client
npm install
npm start
```

Start scala backend:

```shell
cd server
sbt run
```

The game will be accessible on the http://localhost:3001

## Features

1. Isolated rooms allow multiple game sessions at the same times.
2. Three game modes - 24, 36 or 52 cards.
3. Up to 6 players in one room (in 52 cards mode). Minimum is 2.
4. Rooms allow to set password on them. If you want to invite someone - just send them a link to the room, and they'll be prompted to enter the password.
5. Responsive UI allows for it to be played on phones... Kind of.

## Rules

Basically normal "podkidnoy" durak, with some changes. For example, the order of turns is determined by the order of pressing "mark ready" button.

To start the game, at least two people should join the room. After joining, if it was succesful, you'll see buttons on the bottom of the screen. Players that are ready to play should press "mark ready". At any time the owner of the room can start the game by pressing "start game" button, but at least 2 people should be ready. Please note that I did not have time to implement the "visibility" of players who are ready, so either communicate with players or just spam "start game" button until someone presses "mark ready" and it'll start the game.

## Notes

1. The frontend was not a focus of this project, so it's pretty janky. There is no input confirmation on the frontend, and I'm not showing any errors when something goes wrong. They should be visible in the web console though.
2. I think the part of code where I work with fs2 streams is pretty terrible. It was a very unfamiliar concept to me and I didn't have time to figure them out properly, so I may have done something super wrong. Everything else should be good.
3. There is no winner resolving, so you can't "finish" the game. Maybe I'll implement it later.
4. I've said that I wanted to add bots, but I didn't have time for it either.
