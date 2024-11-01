package org.example.game.controller;

import jakarta.servlet.http.HttpSession;
import org.example.game.model.Game;
import org.example.game.model.User;
import org.example.game.service.EventService;
import org.example.game.service.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GameControllerTest {

    private EventService eventService;
    private GameService gameService;
    private GameController gameController;
    private HttpSession session;

    @BeforeEach
    void setUp() {
        eventService = mock(EventService.class);
        gameService = mock(GameService.class);
        gameController = new GameController(eventService, gameService);
        session = mock(HttpSession.class);
    }

    @Test
    void indexShouldGenerateClientIdIfNotPresent() {
        when(session.getAttribute("clientId")).thenReturn(null);
        String view = gameController.index(session);
        verify(session, times(1)).setAttribute(eq("clientId"), anyString());
        assertEquals("index.html", view);
    }

    @Test
    void indexShouldNotGenerateClientIdIfPresent() {
        when(session.getAttribute("clientId")).thenReturn("existingClientId");
        String view = gameController.index(session);
        verify(session, never()).setAttribute(eq("clientId"), anyString());
        assertEquals("index.html", view);
    }

    @Test
    void connectShouldReturnBadRequestIfClientIdNotPresent() {
        when(session.getAttribute("clientId")).thenReturn(null);
        ResponseEntity<SseEmitter> response = gameController.connect(session);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void connectShouldReturnEmitterIfClientIdPresent() {
        when(session.getAttribute("clientId")).thenReturn("clientId");
        when(gameService.createGame("clientId")).thenReturn(new Game(new User("clientId", "User", "X")));
        when(eventService.connect("clientId")).thenReturn(new SseEmitter(Long.MAX_VALUE));
        ResponseEntity<SseEmitter> response = gameController.connect(session);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void makeMoveShouldReturnBadRequestIfClientNotConnected() {
        when(session.getAttribute("clientId")).thenReturn("clientId");
        when(gameService.getUser("clientId")).thenReturn(null);
        ResponseEntity<String> response = gameController.makeMove(0, session);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void makeMoveShouldUpdateGameIfClientConnected() {
        when(session.getAttribute("clientId")).thenReturn("clientId");
        User user = new User("opponentId", "Opponent", "X");
        Game game = new Game(new User("clientId", "User", "O"));
        user.joinGame(game);
        when(gameService.getUser("clientId")).thenReturn(user);
        when(eventService.isClientConnected("clientId")).thenReturn(true);
        ResponseEntity<String> response = gameController.makeMove(0, session);
        verify(gameService, times(1)).makeMove(game);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void randomShouldReturnBadRequestIfClientNotConnected() {
        when(session.getAttribute("clientId")).thenReturn("clientId");
        when(gameService.getUser("clientId")).thenReturn(null);
        ResponseEntity<String> response = gameController.random(session);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void randomShouldReturnNotFoundIfNoOpponentFound() {
        when(session.getAttribute("clientId")).thenReturn("clientId");
        User user = new User("clientId", "User", "X");
        when(gameService.getUser("clientId")).thenReturn(user);
        when(eventService.isClientConnected("clientId")).thenReturn(true);
        when(gameService.getRandmoUser("clientId")).thenReturn(null);
        ResponseEntity<String> response = gameController.random(session);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void randomShouldReturnOkIfOpponentFound() {
        when(session.getAttribute("clientId")).thenReturn("clientId");
        Game game1 = new Game(new User("clientId", "User", "X"));
        Game game2 = new Game(new User("opponentId", "Opponent", "O"));
        when(gameService.getUser("clientId")).thenReturn(game1.users.getFirst());
        when(eventService.isClientConnected("clientId")).thenReturn(true);
        when(gameService.getRandmoUser("clientId")).thenReturn(game2.users.getFirst());
        ResponseEntity<String> response = gameController.random(session);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void inviteShouldReturnBadRequestIfUserNotConnected() {
        when(session.getAttribute("clientId")).thenReturn("clientId");
        User user = new User("clientId", "User", "X");
        when(gameService.getUser("clientId")).thenReturn(user);
        when(eventService.isClientConnected("clientId")).thenReturn(true);
        when(gameService.getUser("userId")).thenReturn(null);
        ResponseEntity<String> response = gameController.invite(session, "userId");
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void acceptShouldReturnNoContentIfClientConnected() {
        when(session.getAttribute("clientId")).thenReturn("clientId");
        Game game = new Game(new User("clientId", "User", "×"));
        User user = new User("opponentId", "Opponent", "○");
        game.setUserOnHold(user);
        when(gameService.getUser("clientId")).thenReturn(game.users.getFirst());
        when(eventService.isClientConnected("clientId")).thenReturn(true);
        ResponseEntity<String> response = gameController.accept(session);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void leaveShouldReturnNoContentIfClientConnected() {
        when(session.getAttribute("clientId")).thenReturn("clientId");
        Game game = new Game(new User("clientId", "User", "×"));
        User user = new User("opponentId", "Opponent", "○");
        user.joinGame(game);
        when(gameService.getUser("clientId")).thenReturn(game.users.getFirst());
        when(gameService.getGame("clientId")).thenReturn(game);
        when(eventService.isClientConnected("clientId")).thenReturn(true);
        ResponseEntity<String> response = gameController.leave(session);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void renameShouldReturnOkIfClientConnected() {
        when(session.getAttribute("clientId")).thenReturn("clientId");
        User user = new User("clientId", "User", "X");
        when(gameService.getUser("clientId")).thenReturn(user);
        when(eventService.isClientConnected("clientId")).thenReturn(true);
        ResponseEntity<String> response = gameController.rename("<script>newName", session);
        assertTrue(response.hasBody());
        assertFalse(response.getBody().contains("<script>"));
        assertFalse(user.getName().contains("<script>"));
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}