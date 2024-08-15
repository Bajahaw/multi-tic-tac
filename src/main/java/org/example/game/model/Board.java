package org.example.game.model;

public class Board {
    //----------------------------------
    private final String[] grid;
    //----------------------------------
    public Board() {
        this.grid = new String[]{"","","","","","","","",""};
    }

    public String[] getGrid() {
        return grid;
    }

    public void update(int id, String symbol) {
        grid[id] = symbol;
    }

    public boolean isFull() {
        for (String cell : grid) if (cell.isEmpty()) return false;
        return true;
    }
}
