package org.example.game.service;

import org.example.game.model.Game;
import org.example.game.model.gameStatus;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.yaml.snakeyaml.emitter.Emitter;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventService {
    //-------------------------------------------------------------------------------------
    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    //-------------------------------------------------------------------------------------
    public void sendEvent(String data) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("update").data(data));
            } catch (Exception e) {
                emitter.completeWithError(e);
                emitters.remove(emitter);
            }
        }
    }
    public SseEmitter connect(){
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        return emitter;
    }

    public void sendInitialState(String[] state) {
        try {
            for (int i = 0; i < 9; i++) {
                emitters.getFirst().send(SseEmitter.event().name("cellUpdate:" + i).data(state[i]));
            }
        } catch (IOException e) {
            emitters.getFirst().completeWithError(e);
            emitters.remove(emitters.getFirst());
        }
    }

    public void broadcastMove(int cellIndex, String value) {
        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event().name("cellUpdate:" + cellIndex).data(value));
            } catch (IOException e) {
                emitter.completeWithError(e);
                emitters.remove(emitter);
            }
        });
    }

    public void broadcastGameStatus(double score1, double score2) {
        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event().name("player:1").data(score1));
                emitter.send(SseEmitter.event().name("player:2").data(score2));
            } catch (IOException e) {
                emitter.completeWithError(e);
                emitters.remove(emitter);
            }
        });
    }

    public void broadcastWinner(String move, int[] winningLine) {
        for (int i = 0; i < 3; i++) {
            String data = "<div class=\"xo" + " blink" + "\">" + move + "</div>";
            try {
                emitters.getFirst().send(SseEmitter.event().name("cellUpdate:" + winningLine[i]).data(data));
            } catch (IOException e) {
                emitters.getFirst().completeWithError(e);
                emitters.remove(emitters.getFirst());
            }
        }
    }
}
