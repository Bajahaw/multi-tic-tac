package org.example.game.model;

public class User {
    //-------------------------
    private final String id;
    private String name;
    private String symbol;
    private Game game;
    private int move = -1;
    private double score = 0;
    //--------------------------

    public User(String id, String name, String symbol) {
        this.id = id;
        this.name = name;
        this.symbol = symbol;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        if (game.getUserToMove() == this && (cellIndex < 9 && cellIndex > -1))
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

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
