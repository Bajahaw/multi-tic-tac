package org.example.game.service;

import org.example.game.model.GameState;
import org.springframework.stereotype.Service;

@Service
public class GameService {

    private final GameState gameState = new GameState();

    public GameState getGameState() {
        return gameState;
    }

    public void reset(){
        gameState.reset();
    }

    public boolean makeMove(int id) {
        boolean moved = gameState.update(id);
        if(moved) gameState.updateStatus();
        if(!gameEnded()) {
            gameState.update(gameState.computerMove(gameState.evaluateBoard()));
            gameState.updateStatus();
        }
        return moved;
    }

    public boolean gameEnded() {
        return !gameState.status.isEmpty();
    }

}