package org.example.game.service;

import org.example.game.model.GameState;
import org.springframework.stereotype.Service;

@Service
public class GameService {

    private GameState gameState = new GameState();

    public GameState makeMove(int id) {
        gameState.update(id);
        return gameState;
    }
}