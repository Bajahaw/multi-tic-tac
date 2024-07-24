package org.example.game.model;

import java.util.Arrays;

public class Game {

    //-------------------------------------------
    private final Board board;
    public User playerOne;
    public User playerTwo;
    boolean playerOneTurn = true;
    public double pOneScore = 0;
    public double pTwoScore = 0;
    public gameStatus status;
    public int[] winningLine;
    public int lastMove;
    //-------------------------------------------

    public Game(User playerOne, User playerTwo) {
        this.board = new Board();
        this.playerOne = playerOne;
        this.playerTwo = playerTwo;
        this.status = gameStatus.IN_PROGRESS;
        playerOne.joinGame(this);
        playerTwo.joinGame(this);
    }

    public boolean makeMove() {
        User user = playerOneTurn? playerOne : playerTwo;
        int move = user.getMove();
        if(move == -1) return false;
        if(status == gameStatus.IN_PROGRESS && getBoard()[move].isEmpty()){
            board.update(move, user.getSymbol());
            lastMove = move;
            playerOneTurn = user != playerOne;
            return true;
        }
        return false;
    }

    public String[] getBoard() {
        return board.grid;
    }

    public void reset() {
        Arrays.fill(board.grid, "");
        playerOneTurn = true;
        status = gameStatus.IN_PROGRESS;
    }

    public gameStatus getStatus() {
        winningLine = checkWinner();
        if (winningLine == null) {
            if (board.isFull()) return gameStatus.DRAW;
            return gameStatus.IN_PROGRESS;
        }
        return board.grid[winningLine[0]] == playerOne.getSymbol() ? gameStatus.PLAYER_ONE_WON : gameStatus.PLAYER_TWO_WON;
    }

    public void updateStatus() {
        this.status = getStatus();
        switch (status) {
            case gameStatus.DRAW:
                pOneScore += 0.5;
                pTwoScore += 0.5;
                break;
            case gameStatus.PLAYER_ONE_WON:
                pOneScore += 1;
                break;
            case gameStatus.PLAYER_TWO_WON:
                pTwoScore += 1;
                break;
        }
    }

    private int[] checkWinner() {
        int[][] lines = {{0, 1, 2}, {3, 4, 5}, {6, 7, 8}, {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, {0, 4, 8}, {2, 4, 6}};
        for (int[] line : lines) {
            if (board.grid[line[0]] != ""
                    && board.grid[line[0]] == board.grid[line[1]]
                    && board.grid[line[1]] == board.grid[line[2]]) {
                return line;
            }
        }
        return null;
    }


}
