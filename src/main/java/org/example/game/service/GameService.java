package org.example.game.service;

import org.example.game.model.Game;
import org.example.game.model.GameServer;
import org.example.game.model.gameStatus;
import org.springframework.stereotype.Service;

@Service
public class GameService {

    private final Game game = GameServer.createGame();

    public Game getGame() {
        return game;
    }

    public void reset(){
        game.reset();
    }

    public boolean makeMove(int id) {
        boolean moved = game.makeMove(id);
        if(moved) game.updateStatus();
        if(!gameEnded()) {
            game.makeMove(game.computerMove);
            game.updateStatus();
        }
        return moved;
    }

    public boolean gameEnded() {
        return !(game.status == gameStatus.IN_PROGRESS);
    }

}