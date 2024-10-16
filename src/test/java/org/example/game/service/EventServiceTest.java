package org.example.game.service;
import org.example.game.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EventServiceTest {

    @Test
    void connectShouldAddEmitter() {
        EventService eventService = new EventService();
        SseEmitter emitter = eventService.connect("user1");
        assertNotNull(emitter);
        assertTrue(eventService.isClientConnected("user1"));
    }

    @Test
    void connectShouldRemoveEmitterOnError() {
        EventService eventService = new EventService();
        SseEmitter emitter = eventService.connect("user1");
        emitter.completeWithError(new IOException());
        assertFalse(eventService.isClientConnected("user1"));
    }

    @Test
    void sendInitialStateShouldSendEvents() throws IOException {
        EventService eventService = new EventService();
        User user = new User("user1", "User One", "X");
        user.setGame(new Game(user));
        eventService.connect(user.getId());
        String[] state = {"X", "O", "X", "O", "X", "O", "X", "O", "X"};
        eventService.sendInitialState(state, List.of(user));
        assertTrue(eventService.isClientConnected(user.getId()));
    }

    @Test
    void broadcastMoveShouldSendEventToAllUsers() throws IOException {
        EventService eventService = new EventService();
        User user1 = new User("user1", "User One", "X");
        User user2 = new User("user2", "User Two", "O");
        eventService.connect(user1.getId());
        eventService.connect(user2.getId());
        eventService.broadcastMove(0, "X", List.of(user1, user2));
        assertTrue(eventService.isClientConnected(user1.getId()));
        assertTrue(eventService.isClientConnected(user2.getId()));
    }

    @Test
    void broadcastGameStatusShouldSendScoresToAllUsers() throws IOException {
        EventService eventService = new EventService();
        User user1 = new User("user1", "User One", "X");
        User user2 = new User("user2", "User Two", "O");
        eventService.connect(user1.getId());
        eventService.connect(user2.getId());
        eventService.broadcastGameStatus(1.0, 2.0, List.of(user1, user2));
        assertTrue(eventService.isClientConnected(user1.getId()));
        assertTrue(eventService.isClientConnected(user2.getId()));
    }

    @Test
    void broadcastWinnerShouldSendWinningLineToAllUsers() throws IOException {
        EventService eventService = new EventService();
        User user1 = new User("user1", "User One", "X");
        User user2 = new User("user2", "User Two", "O");
        eventService.connect(user1.getId());
        eventService.connect(user2.getId());
        int[] winningLine = {0, 1, 2};
        eventService.broadcastWinner("X", winningLine, List.of(user1, user2));
        assertTrue(eventService.isClientConnected(user1.getId()));
        assertTrue(eventService.isClientConnected(user2.getId()));
    }

    @Test
    void sendEventShouldSendEventToSpecificUser() throws IOException {
        EventService eventService = new EventService();
        User user = new User("user1", "User One", "X");
        eventService.connect(user.getId());
        eventService.sendEvent(user.getId(), "testEvent", "testData");
        assertTrue(eventService.isClientConnected(user.getId()));
    }

    @Test
    void isClientConnectedShouldReturnFalseForDisconnectedUser() {
        EventService eventService = new EventService();
        assertFalse(eventService.isClientConnected("nonexistentUser"));
    }

    @Test
    void disConnectShouldRemoveEmitter() {
        EventService eventService = new EventService();
        eventService.connect("user1");
        eventService.disConnect("user1");
        assertFalse(eventService.isClientConnected("user1"));
    }
}