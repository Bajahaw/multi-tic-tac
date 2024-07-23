package org.example.game.model;

public class User {
    //-------------------------
    public static int index = 0;
    protected final int id;
    protected final String name;
    protected String symbol;
    protected Game game;
    protected int move = -1;
    protected final double score = 0;
    //--------------------------

    public User(int id, String name, String symbol) {
        this.id = id;
        this.name = name;
        this.symbol = symbol;
        index++;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getSymbol() { return symbol; }
    public double getScore() { return score; }

    public int getMove(){
        int temp = move;
        move = -1;
        return temp;
    }

    public void setMove(int cellIndex) {
        move = cellIndex;
    }

    public void joinGame(Game game) {
        this.game = game;
    }
}
