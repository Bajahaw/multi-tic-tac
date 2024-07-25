package org.example.game.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Game {

    //-------------------------------------------
    private final Board board;
    public final List<User> users = new ArrayList<>();
    User userToMove;
    public double pOneScore = 0;
    public double pTwoScore = 0;
    public gameStatus status;
    public int[] winningLine;
    public int lastMove;
    //-------------------------------------------

    public Game(User playerOne) {
        this.board = new Board();
        this.users.add(playerOne);
        this.status = gameStatus.IN_PROGRESS;
        playerOne.joinGame(this);
        userToMove = playerOne;
    }

    public void setPlayerTwo(User playerTwo) {
        playerTwo.symbol = this.users.getFirst().getSymbol() == "○"? "×":"○";
        this.users.add(playerTwo);
        playerTwo.joinGame(this);
    }

    public boolean makeMove() {
        User user = userToMove;
        int move = user.getMove();
        System.out.println(user.getId() + " -> " + move);
        if(move == -1) return false;
        if(status == gameStatus.IN_PROGRESS && getBoard()[move].isEmpty()){
            board.update(move, user.getSymbol());
            lastMove = move;
            userToMove = user == users.getFirst()? users.getLast() : users.getFirst();
            return true;
        }
        return false;
    }

    public String[] getBoard() {
        return board.grid;
    }

    public void reset() {
        Arrays.fill(board.grid, "");
        status = gameStatus.IN_PROGRESS;
    }

    public gameStatus getStatus() {
        winningLine = checkWinner();
        if (winningLine == null) {
            if (board.isFull()) return gameStatus.DRAW;
            return gameStatus.IN_PROGRESS;
        }
        return board.grid[winningLine[0]] == users.getFirst().getSymbol() ? gameStatus.PLAYER_ONE_WON : gameStatus.PLAYER_TWO_WON;
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
