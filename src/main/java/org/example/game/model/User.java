package org.example.game.model;

public class User {
    public static int index = 0;
    private final int id;
    private final String name;
    private String symbol;
    public User(int id, String name, String symbol) {
        this.id = id;
        this.name = name;
        this.symbol = symbol;
        index++;
    }
    public int getId() { return id; }
    public String getName() { return name; }
    public String getSymbol() { return symbol; }
}
