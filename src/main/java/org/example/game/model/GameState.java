package org.example.game.model;

public class GameState {
    private String[] state;
    String playerOne = "X";
    String playerTwo = "O";
    boolean playerOneTurn = true;

    public GameState() {
        this.state = new String[]{"", "", "", "", "", "", "", "", ""};
    }

    public String toHtml() {
        return new StringBuilder().append("""
                    <div id="0" class="square top left" hx-get="/update-content" hx-trigger="click" hx-target=".board">
                        <div class="xo">""").append(state[0]).append("""
                    </div>
                    </div>
                    <div id="1" class="square top" hx-get="/update-content" hx-trigger="click" hx-target=".board">
                        <div class="xo">""").append(state[1]).append("""
                    </div>
                    </div>
                    <div id="2" class="square top right" hx-get="/update-content" hx-trigger="click" hx-target=".board">
                        <div class="xo">""").append(state[2]).append("""
                    </div>
                    </div>
                    <div id="3" class="square left" hx-get="/update-content" hx-trigger="click" hx-target=".board">
                        <div class="xo">""").append(state[3]).append("""
                        </div>
                    </div>
                    <div id="4" class="square" hx-get="/update-content" hx-trigger="click" hx-target=".board">
                        <div class="xo">""").append(state[4]).append("""
                        </div>
                    </div>
                    <div id="5" class="square right" hx-get="/update-content" hx-trigger="click" hx-target=".board">
                        <div class="xo">""").append(state[5]).append("""
                    </div>
                    </div>
                    <div id="6" class="square bottom left" hx-get="/update-content" hx-trigger="click" hx-target=".board">
                        <div class="xo">""").append(state[6]).append("""
                    </div>
                    </div>
                    <div id="7" class="square bottom" hx-get="/update-content" hx-trigger="click" hx-target=".board">
                        <div class="xo">""").append(state[7]).append("""
                    </div>
                    </div>
                    <div id="8" class="square bottom right" hx-get="/update-content" hx-trigger="click" hx-target=".board">
                        <div class="xo">""").append(state[8]).append("""
                    </div>
                    </div>
                """).toString();
    }

    public void update(int id) {
        if (playerOneTurn) {
            state[id] = playerOne;
            playerOneTurn = false;
        }
        else {
            playerOneTurn = true;
            state[id] = playerTwo;
        }
    }

    // Getter and Setter methods
}