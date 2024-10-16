package org.example.game.service;

import org.example.game.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameService {
    //-------------------------------------------------
    EventService eventService;
    private final Map<String, Game> activeGames = new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(GameService.class);
    //-------------------------------------------------

    public GameService(EventService eventService) {
        this.eventService = eventService;
    }

    public Game createGame(String id) {
        Game game = new Game( new User(id, "player" + id, "×"));
        activeGames.put(id, game);
        return game;
    }

    public Game getGame(String id) {
        if (id == null) return null;
        return activeGames.get(id);
    }

    public User getUser(String id) {
        Game game = getGame(id);
        if (game == null) return null;
        return game.users.getFirst();
    }

    public void reset(Game game) {
        game.reset();
        eventService.sendInitialState(game.getBoard(), game.users);
    }

    public boolean gameEnded(Game game) {
        return !(game.status == GameStatus.IN_PROGRESS);
    }

    public void makeMove(Game game) {
        game.setLastActivityTime(LocalDateTime.now());
        boolean moved = game.makeMove();
        if (moved) {
            game.updateStatus();
            String move = game.getBoard()[game.lastMove];
            eventService.broadcastMove(game.lastMove, "<div class=\"xo\">" + move + "</div>", game.users);

            if (game.users.size() < 2 && !gameEnded(game)) {
                Computer.makeMove(game);
                move = game.getBoard()[game.lastMove];
                eventService.broadcastMove(game.lastMove, "<div class=\"xo\">" + move + "</div>", game.users);
                game.updateStatus();
            }
            if (gameEnded(game)) {
                eventService.broadcastGameStatus(game.pOneScore, game.pTwoScore, game.users);
                if (!(game.status == GameStatus.DRAW)) {
                    eventService.broadcastWinner(game.getBoard()[game.winningLine[0]], game.winningLine, game.users);
                }
            }

        } else if (gameEnded(game)) {
            reset(game);
        }
    }

    @Scheduled(fixedRate = 600000)
    public void removeInactiveGames() {
        logger.info("Active games: {}", activeGames.size());

        LocalDateTime now = LocalDateTime.now();
        Iterator<Game> iterator = activeGames.values().iterator();
        while (iterator.hasNext()) {
            Game game = iterator.next();
            if (game.isInactive(now)) {
                eventService.disConnect(game.users.getFirst().getId());
                iterator.remove();
            }
        }
    }

    public User getRandmoUser(String id) {
        return activeGames
                .values()
                .stream().filter(c ->

                                c.users.size()<2 &&
                                c.getUserOnHold() == null &&
                                !c.users.getFirst().getId().equals(id) &&
                                eventService.isClientConnected(c.users.getFirst().getId()))

                .findFirst()
                .map(c -> c.users.getFirst())
                .orElse(null);
    }
}