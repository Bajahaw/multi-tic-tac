package org.example.game.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Game {

    //-------------------------------------------
    private final Board board;
    public final List<User> users = new ArrayList<>();
    private User userToMove;
    private User userOnHold;
    public double pOneScore = 0;
    public double pTwoScore = 0;
    public GameStatus status;
    public int[] winningLine;
    private LocalDateTime lastActivityTime;
    public int lastMove;
    //-------------------------------------------

    public Game(User playerOne) {
        this.board = new Board();
        this.users.add(playerOne);
        this.status = GameStatus.IN_PROGRESS;
        this.lastActivityTime = LocalDateTime.now();
        playerOne.joinGame(this);
        userToMove = users.getFirst();
    }

    public void setPlayerTwo(User playerTwo) {
        // player joining himself -> remove the second player
        if (playerTwo == users.getFirst()) {
            if (users.size() > 1) users.remove(users.getLast());
            userToMove = users.getFirst();
            userOnHold = null;
            reset();
            resetScore();
            return;
        }
        playerTwo.setSymbol(this.users.getFirst().getSymbol().equals("○") ? "×" : "○");
        this.users.add(playerTwo);
        playerTwo.joinGame(this);
        reset();
        resetScore();
    }

    private void resetScore() {
        pOneScore = 0;
        pTwoScore = 0;
    }

    public boolean makeMove() {
        User user = userToMove;
        int move = user.getMove();

        if (move == -1) return false;
        if (status == GameStatus.IN_PROGRESS && getBoard()[move].isEmpty()) {
            board.update(move, user.getSymbol());
            lastMove = move;
            userToMove = user == users.getFirst() ? users.getLast() : users.getFirst();
            return true;
        }
        return false;
    }

    public String[] getBoard() {
        return board.getGrid();
    }

    public void reset() {
        for(int i=0; i<board.getGrid().length; i++)
            board.update(i, "");
        status = GameStatus.IN_PROGRESS;
    }

    public GameStatus getStatus() {
        winningLine = checkWinner();
        if (winningLine == null) {
            if (board.isFull()) return GameStatus.DRAW;
            return GameStatus.IN_PROGRESS;
        }
        return board.getGrid()[winningLine[0]].equals(users.getFirst().getSymbol()) ? GameStatus.PLAYER_ONE_WON : GameStatus.PLAYER_TWO_WON;
    }

    public void updateStatus() {
        this.status = getStatus();
        switch (status) {
            case GameStatus.DRAW:
                pOneScore += 0.5;
                pTwoScore += 0.5;
                break;
            case GameStatus.PLAYER_ONE_WON:
                pOneScore += 1;
                break;
            case GameStatus.PLAYER_TWO_WON:
                pTwoScore += 1;
                break;
        }
    }

    private int[] checkWinner() {
        int[][] lines = {{0, 1, 2}, {3, 4, 5}, {6, 7, 8}, {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, {0, 4, 8}, {2, 4, 6}};
        for (int[] line : lines) {
            if (!board.getGrid()[line[0]].isEmpty()
                    && board.getGrid()[line[0]].equals(board.getGrid()[line[1]])
                    && board.getGrid()[line[1]].equals(board.getGrid()[line[2]])) {
                return line;
            }
        }
        return null;
    }

    public void setLastActivityTime(LocalDateTime lastActivityTime) {
        this.lastActivityTime = lastActivityTime;
    }

    public boolean isInactive(LocalDateTime now) {
        return lastActivityTime.isBefore(now.minusMinutes(10));
    }

    public User getUserToMove() {
        return userToMove;
    }

    public User getUserOnHold() {
        return userOnHold;
    }

    public void setUserOnHold(User userOnHold) {
        this.userOnHold = userOnHold;
    }
}
