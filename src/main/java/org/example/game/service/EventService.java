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
        emitter.onCompletion(() -> emitters.remove(user));
        emitter.onTimeout(() -> emitters.remove(user));
        emitter.onError(e -> emitters.remove(user));
        return emitter;
    }

    public void sendInitialState(String[] state, List<User> users) {
        for (User user : users) {
            if (emitters.containsKey(user.getId())) {
                try {
                    for (int i = 0; i < 9; i++) {
                        emitters.get(user.getId()).send(SseEmitter.event().name("cellUpdate:" + i).data("<div class=\"xo\">" + state[i] + "</div>"));
                    }

                    broadcastGameStatus(user.getGame().pOneScore, user.getGame().pTwoScore, List.of(user));

                    String name = users.size() < 2 ? "Computer" : "player" + (users.getFirst().getId().equals(user.getId()) ? users.getLast().getId() : users.getFirst().getId());
                    emitters.get(user.getId()).send(SseEmitter.event().name("player2name").data(name));

                } catch (IOException e) {
                    System.out.println("sendEvent error: Connection with: " + user.getId() + " might be lost -> " + e.getMessage());
                    emitters.remove(user.getId());
                }
            }
        }
    }

    public void broadcastMove(int cellIndex, String value, List<User> users) {
        for (User user : users) {
            try {
                if (emitters.containsKey(user.getId()))
                    emitters.get(user.getId()).send(SseEmitter.event().name("cellUpdate:" + cellIndex).data(value));
            } catch (IOException e) {
                System.out.println("sendEvent error: Connection with: " + user.getId() + " might be lost -> " + e.getMessage());
                emitters.remove(user.getId());
            }
        }
    }

    public void broadcastGameStatus(double score1, double score2, List<User> users) {
        for (User user : users) {
            try {
                if (emitters.containsKey(user.getId())) {
                    emitters.get(user.getId()).send(SseEmitter.event().name("player:1").data(score1));
                    emitters.get(user.getId()).send(SseEmitter.event().name("player:2").data(score2));
                }
            } catch (IOException e) {
                System.out.println("sendEvent error: Connection with: " + user.getId() + " might be lost -> " + e.getMessage());
                emitters.remove(user.getId());
            }
        }
    }

    public void broadcastWinner(String move, int[] winningLine, List<User> users) {
        for (User user : users) {
            for (int i = 0; i < 3; i++) {
                String data = "<div class=\"xo" + " blink" + "\">" + move + "</div>";
                try {
                    if (emitters.containsKey(user.getId()))
                        emitters.get(user.getId()).send(SseEmitter.event().name("cellUpdate:" + winningLine[i]).data(data));
                } catch (IOException e) {
                    System.out.println("sendEvent error: Connection with: " + user.getId() + " might be lost -> " + e.getMessage());
                    emitters.remove(user.getId());
                }
            }
        }
    }

    public void sendEvent(String id, String eventName, String data) {
        try {
            if (emitters.containsKey(id))
                emitters.get(id).send(SseEmitter.event().name(eventName).data(data));
        } catch (IOException e) {
            System.out.println("sendEvent error: Connection with: " + id + " might be lost -> " + e.getMessage());
            emitters.remove(id);
        }
    }

    public boolean isClientConnected(String id) {
        SseEmitter emitter = emitters.get(id);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("heartbeat"));
                return true;
            } catch (IOException e) {
                System.out.println("sendEvent error: Connection with: " + id + " might be lost -> " + e.getMessage());
                emitters.remove(id);
                return false;
            }
        }
        return false;
    }

    public void disConnect(String id) {
        emitters.remove(id);
    }
}
