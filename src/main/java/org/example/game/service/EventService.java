package org.example.game.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class EventService {
    //-------------------------------------------------------------------------------------
    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    //-------------------------------------------------------------------------------------

    public SseEmitter connect(){
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> System.out.println("SSE connection timed out"));
        emitter.onError(e -> emitters.remove(emitter));
        return emitter;
    }

        public void sendInitialState(String[] state) {
            emitters.forEach(emitter -> {
                try {
                    for (int i = 0; i < 9; i++) {
                        emitters.getFirst().send(SseEmitter.event().name("cellUpdate:" + i).data(state[i]));
                    }
                } catch (IOException e) {
                    emitters.remove(emitter);
                }
            });
        }

    public void broadcastMove(int cellIndex, String value) {
        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event().name("cellUpdate:" + cellIndex).data(value));
            } catch (IOException e) {
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
                emitters.remove(emitters.getFirst());
            }
        }
    }
    // This is here to temporarily fix a broken pipe exception
    public void reloadSse(){
        if(emitters.size()>1)
            emitters.removeFirst();
    }
}
