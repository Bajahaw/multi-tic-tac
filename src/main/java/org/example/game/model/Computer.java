package org.example.game.model;

public class Computer {

    public static void makeMove(Game game) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        String symbol = game.users.getFirst().getSymbol().equals("○") ? "×" : "○";
        game.lastMove = computerMove(game.getBoard(), evaluateBoard(game.getBoard(), symbol));
        game.getBoard()[game.lastMove] = symbol;
    }

    private static int computerMove(String[] board, int[] combination) {
        for (int i = 0; i < 3; i++) {
            if (board[combination[i]].isEmpty()) {
                return combination[i];
            }
        }
        return -1;
    }

    private static int[] evaluateBoard(String[] board, String symbol) {
        int winEvaluation;
        int loseEvaluation;
        int highestWinEvaluation = 0;
        int highestLoseEvaluation = 0;
        int[] bestCompination = new int[3];
        int[] worstCompination = new int[3];
        int[][] combinations = {
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
                    if (board[combinations[i][j]].equals(symbol)) {
                        counter++;
                        loseEvaluation = 0;
                        winEvaluation++;
                        if (winEvaluation >= highestWinEvaluation && counter < 3) {
                            highestWinEvaluation = winEvaluation;
                            bestCompination = combinations[i];
                        }
                    }
                    if (board[combinations[i][j]].equals(symbol.equals("○") ? "×" : "○")) {
                        counter++;
                        winEvaluation = 0;
                        loseEvaluation++;
                        if (loseEvaluation >= highestLoseEvaluation && counter < 3) {
                            highestLoseEvaluation = loseEvaluation;
                            worstCompination = combinations[i];
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
}
