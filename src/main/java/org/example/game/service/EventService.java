package org.example.game.service;

import org.example.game.model.User;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class EventService {
    //-------------------------------------------------------------------------------------
    private final ConcurrentMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    //-------------------------------------------------------------------------------------

    public SseEmitter connect(String user) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        System.out.println("Connecting to user " + user);
        emitters.put(user, emitter);
        //System.out.println(emitters.size());
        emitter.onCompletion(() -> emitters.remove(user));
        emitter.onTimeout(() -> emitters.remove(user));
        emitter.onError(e -> emitters.remove(user));
        return emitter;
    }

    public void sendInitialState(String[] state, List<User> users) {
        for (User user : users) {
            try {
                for (int i = 0; i < 9; i++) {
                    System.out.println("sending state " + i + " to user " + user.getId());
                    emitters.get(user.getId()).send(SseEmitter.event().name("cellUpdate:" + i).data("<div class=\"xo\">" + state[i] + "</div>"));
                }
            } catch (IOException e) {
                emitters.remove(user.getId());
            }
        }
    }

    public void broadcastMove(int cellIndex, String value, List<User> users) {
        for (User user : users) {
            try {
                System.out.println("sse emits to: " + user.getId());
                System.out.println("size: " + emitters.size());
                emitters.get(user.getId()).send(SseEmitter.event().name("cellUpdate:" + cellIndex).data(value));
            } catch (IOException e) {
                emitters.remove(user.getId());
            }
        }
    }

    public void broadcastGameStatus(double score1, double score2, List<User> users) {
        for (User user : users) {
            try {
                System.out.println("sse emits to: " + user.getId());
                emitters.get(user.getId()).send(SseEmitter.event().name("player:1").data(score1));
                emitters.get(user.getId()).send(SseEmitter.event().name("player:2").data(score2));
            } catch (IOException e) {
                emitters.remove(user.getId());
            }
        }
    }

    public void broadcastWinner(String move, int[] winningLine, List<User> users) {
        for (User user : users) {
            for (int i = 0; i < 3; i++) {
                String data = "<div class=\"xo" + " blink" + "\">" + move + "</div>";
                try {
                    System.out.println("winner emits to: " + user.getId());
                    emitters.get(user.getId()).send(SseEmitter.event().name("cellUpdate:" + winningLine[i]).data(data));
                } catch (IOException e) {
                    emitters.remove(user.getId());
                }
            }
        }
    }

    public void notifyPlayer(String id, String clientId) {
        try {
            emitters.get(id).send(SseEmitter.event().name("notifications").data(clientId));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
