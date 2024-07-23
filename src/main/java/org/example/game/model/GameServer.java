package org.example.game.model;

import java.util.ArrayList;
import java.util.List;

public class GameServer {
    static List<Game> activeGames = new ArrayList<>();

    public static Game createGame(){
        Game game = new Game(new User(User.index, "Random User", "○"), new Computer(User.index, "Computer", "×"));
        activeGames.add(game);
        return game;
    }
}
