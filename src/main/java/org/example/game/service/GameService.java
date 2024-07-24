package org.example.game.service;

import org.example.game.model.Computer;
import org.example.game.model.Game;
import org.example.game.model.GameServer;
import org.example.game.model.gameStatus;
import org.springframework.stereotype.Service;

@Service
public class GameService {
    //-------------------------------------------------
    EventService eventService;
    private final Game game = GameServer.createGame();
    //-------------------------------------------------

    public GameService(EventService eventService){
        this.eventService = eventService;
    }

    public Game getGame() {
        return game;
    }
    public void reset(){
        game.reset();
        eventService.sendInitialState(game.getBoard());
    }
    public boolean gameEnded() {
        return !(game.status == gameStatus.IN_PROGRESS);
    }

    public boolean makeMove() {
        boolean moved = game.makeMove();
        String move;
        if (moved) {
            game.updateStatus();
            move = game.getBoard()[game.lastMove];
            eventService.broadcastMove(game.lastMove, "<div class=\"xo\">" + move + "</div>");

            if(game.playerTwo instanceof Computer && !gameEnded()) return makeMove();
            if (gameEnded()) {
                eventService.broadcastGameStatus(getGame().pOneScore, getGame().pTwoScore);
                if(!(game.status == gameStatus.DRAW)) {
                    eventService.broadcastWinner(game.getBoard()[game.winningLine[0]], game.winningLine);
                }
            }

        } else if (gameEnded()) {
            reset();
        }
        return moved;
    }

}