package org.example.game.service;

import org.example.game.model.Game;
import org.example.game.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GameServiceTest {

    private EventService eventService;
    private GameService gameService;

    @BeforeEach
    void setUp() {
        eventService = mock(EventService.class);
        gameService = new GameService(eventService);
    }

    @Test
    void createGameShouldAddGameToActiveGames() {
        Game game = gameService.createGame("user1");
        assertNotNull(game);
        assertEquals(game, gameService.getGame("user1"));
    }

    @Test
    void getUserShouldReturnUserForExistingGame() {
        gameService.createGame("user1");
        User user = gameService.getUser("user1");
        assertNotNull(user);
        assertEquals("user1", user.getId());
    }

    @Test
    void getUserShouldReturnNullForNonExistentGame() {
        assertNull(gameService.getUser("nonexistent"));
    }

    @Test
    void resetShouldResetGameAndSendInitialState() {
        Game game = gameService.createGame("user1");
        gameService.reset(game);
        verify(eventService, times(1)).sendInitialState(game.getBoard(), game.users);
    }

    @Test
    void makeMoveShouldUpdateGameAndBroadcastMove() {
        Game game = gameService.createGame("user1");
        game.users.getFirst().updateMove(0);
        gameService.makeMove(game);
        verify(eventService, atLeastOnce()).broadcastMove(anyInt(), anyString(), eq(game.users));
    }

    @Test
    void removeInactiveGamesShouldRemoveInactiveGames() {
        Game game = gameService.createGame("user1");
        game.setLastActivityTime(LocalDateTime.now().minusMinutes(11));
        gameService.removeInactiveGames();
        assertNull(gameService.getGame("user1"));
    }

    @Test
    void getRandomUserShouldReturnNullIfNoMatchingUser() {
        assertNull(gameService.getRandmoUser("user1"));
    }

    @Test
    void getRandomUserShouldReturnMatchingUser() {
        Game game = gameService.createGame("user1");
        when(eventService.isClientConnected("user1")).thenReturn(true);
        User randomUser = gameService.getRandmoUser("user2");
        assertNotNull(randomUser);
        assertEquals("user1", randomUser.getId());
    }
}