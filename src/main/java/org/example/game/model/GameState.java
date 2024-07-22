package org.example.game.model;

import java.util.Arrays;

public class GameState {
    private final String[] board;
    String playerOne = "×";
    String playerTwo = "○";
    boolean playerOneTurn = true;
    public double pOneScore = 0;
    public double pTwoScore = 0;
    public String status = "";
    public int[] winningLine;
    public int computerMove;

    public GameState() {
        this.board = new String[]{"", "", "", "", "", "", "", "", ""};
    }

    public String toHtml() {
        return new StringBuilder().append("""
                <div id="0" class="square top left" hx-post="/move/0" sse-swap="cellUpdate:0">
                    <div class="xo">""").append(board[0]).append("""
                </div>
                </div>
                <div id="1" class="square top" hx-post="/move/1" sse-swap="cellUpdate:1">
                    <div class="xo">""").append(board[1]).append("""
                </div>
                </div>
                <div id="2" class="square top right" hx-post="/move/2" sse-swap="cellUpdate:2">
                    <div class="xo">""").append(board[2]).append("""
                </div>
                </div>
                <div id="3" class="square left" hx-post="/move/3" sse-swap="cellUpdate:3">
                    <div class="xo">""").append(board[3]).append("""
                    </div>
                </div>
                <div id="4" class="square" hx-post="/move/4" sse-swap="cellUpdate:4">
                    <div class="xo">""").append(board[4]).append("""
                    </div>
                </div>
                <div id="5" class="square right" hx-post="/move/5" sse-swap="cellUpdate:5">
                    <div class="xo">""").append(board[5]).append("""
                </div>
                </div>
                <div id="6" class="square bottom left" hx-post="/move/6" sse-swap="cellUpdate:6">
                    <div class="xo">""").append(board[6]).append("""
                </div>
                </div>
                <div id="7" class="square bottom" hx-post="/move/7" sse-swap="cellUpdate:7">
                    <div class="xo">""").append(board[7]).append("""
                </div>
                </div>
                <div id="8" class="square bottom right" hx-post="/move/8" sse-swap="cellUpdate:8">
                    <div class="xo">""").append(board[8]).append("""
                    </div>
                    </div>
                """).toString();
    }

    public boolean update(int id) {
        if (board[id].isEmpty() && status.isEmpty()) {
            if (playerOneTurn) {
                board[id] = playerOne;
                playerOneTurn = false;
            } else {
                playerOneTurn = true;
                board[id] = playerTwo;
            }
            return true;
        }
        return false;
    }

    public int[] evaluateBoard() {
        int winEvaluation = 0;
        int loseEvaluation = 0;
        int highestWinEvaluation = 0;
        int highestLoseEvaluation = 0;
        int[] bestCompination = new int[3];
        int[] worstCompination = new int[3];
        int[][] compinations = {
                {6, 8, 7},
                {0, 6, 3},
                {2, 8, 5},
                {0, 2, 1},
                {4, 3, 5},
                {4, 1, 7},
                {4, 0, 8},
                {4, 2, 6}
        };

        boolean[] isValid = new boolean[8];
        for (int i = 0; i < 8; i++) {
            isValid[i] = true;
        }

        for (int i = 0; i < 8; i++) {

            winEvaluation = 0;
            loseEvaluation = 0;
            int counter = 0;
            if (isValid[i]) {
                for (int j = 0; j < 3; j++) {
                    if (board[compinations[i][j]] == playerTwo) {
                        counter++;
                        loseEvaluation = 0;
                        winEvaluation++;
                        if (winEvaluation >= highestWinEvaluation && counter < 3) {
                            highestWinEvaluation = winEvaluation;
                            bestCompination = compinations[i];
                        }
                    }
                    if (board[compinations[i][j]] == playerOne) {
                        counter++;
                        winEvaluation = 0;
                        loseEvaluation++;
                        if (loseEvaluation >= highestLoseEvaluation && counter < 3) {
                            highestLoseEvaluation = loseEvaluation;
                            worstCompination = compinations[i];
                        }
                    }
                    if (counter >= 3) {
                        isValid[i] = false;
                        highestLoseEvaluation = 0;
                        highestWinEvaluation = 0;
                        i = -1;
                    }
                }
            }

        }

        if (highestWinEvaluation >= highestLoseEvaluation) {
            return bestCompination;
        } else {
            return worstCompination;
        }

    }

    public int computerMove(int[] combination) {
        for (int i = 0; i < 3; i++) {
            if (board[combination[i]] == "") {
                //board[combination[i]] = playerTwo;
                computerMove = combination[i];
                System.out.println(computerMove);
                return combination[i];
            }
        }
        return -1;
    }


    public String[] getBoard() {
        return board;
    }

    public void reset() {
        Arrays.fill(board, "");
        playerOneTurn = true;
        status = "";
    }

    public String getStatus() {
        winningLine = checkWinner();
        if (winningLine == null) {
            if (isBoardFull()) return "draw";
            return "";
        }
        return board[winningLine[0]];
    }

    public void updateStatus() {
        this.status = getStatus();
        switch (status) {
            case "draw":
                pOneScore += 0.5;
                pTwoScore += 0.5;
                break;
            case "×":
                pOneScore += 1;
                break;
            case "○":
                pTwoScore += 1;
                break;
        }
    }

    private int[] checkWinner() {
        int[][] lines = {{0, 1, 2}, {3, 4, 5}, {6, 7, 8}, {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, {0, 4, 8}, {2, 4, 6}};
        for (int[] line : lines) {
            if (board[line[0]] != "" && board[line[0]] == board[line[1]] && board[line[1]] == board[line[2]]) {
                return line;
            }
        }
        return null;
    }

    private boolean isBoardFull() {
        for (String cell : board) {
            if (cell.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
