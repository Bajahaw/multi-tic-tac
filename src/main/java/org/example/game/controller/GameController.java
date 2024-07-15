package org.example.game.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.game.model.GameState;
import org.example.game.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

@Controller
public class GameController {
    GameService gameService = new GameService();

    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    @GetMapping("/connect")
    public SseEmitter connect() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        sendInitialState(emitter);
        return emitter;
    }
    @PostMapping("/move/{cellIndex}")
    public ResponseEntity<String> makeMove(@PathVariable int cellIndex) {
        String move;
        if(gameService.makeMove(cellIndex)) {
            move = gameService.getGameState().getState()[cellIndex];
            broadcastMove(cellIndex, "<div class=\"xo\">"+move+"</div>");
        }
        if(gameService.gameEnded()) broadcastGameStatus(gameService.getGameState().pOneScore, gameService.getGameState().pTwoScore);
        return ResponseEntity.noContent().build();
    }

    private void broadcastGameStatus(double score1, double score2) {
        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event().name("player:1").data(score1));
                emitter.send(SseEmitter.event().name("player:2").data(score2));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        });
    }

    private void broadcastMove(int cellIndex, String value) {
        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event().name("cellUpdate:" + cellIndex).data(value));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        });
    }

    private void sendInitialState(SseEmitter emitter) {
        try {
            String[] state = gameService.getGameState().getState();
            for (int i = 0; i < 9; i++) {
                if (!Objects.equals(state[i], "")) {
                    emitter.send(SseEmitter.event().name("cellUpdate:" + i).data(state[i]));
                }
            }
            //emitter.send(SseEmitter.event().name("gameStatus").data(gameService.getGameState().getStatus()));
        } catch (IOException e) {
            emitters.remove(emitter);
        }
    }

    @GetMapping("/reset")
    public void reset() {
        gameService.reset();
    }
}
