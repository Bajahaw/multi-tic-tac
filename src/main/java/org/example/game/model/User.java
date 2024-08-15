package org.example.game.model;

import java.util.Random;

public class User {
    //-------------------------
    public static int index = 0;
    protected final String id;
    protected final String name;
    protected String symbol;
    protected Game game;
    protected int move = -1;
    protected final double score = 0;
    //--------------------------

    public User(String id, String name, String symbol) {
        this.id = id;
        this.name = name + Random.from(new Random()).nextInt(10);
        this.symbol = symbol;
        index++;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getScore() {
        return score;
    }

    public int getMove() {
        int temp = move;
        move = -1;
        return temp;
    }

    public void updateMove(int cellIndex) {
        if (game.userToMove == this && (cellIndex < 9 && cellIndex > -1))
            move = cellIndex;
    }

    public void joinGame(Game game) {
        this.game = game;
    }

    public Game getGame() {
        return game;
    }

    public boolean isFree() {
        return game.users.size() < 2;
    }
}
