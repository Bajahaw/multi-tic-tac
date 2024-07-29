package org.example.game.service;

import org.example.game.model.*;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameService {
    //-------------------------------------------------
    EventService eventService;
    private final Map<String, Game> activeGames = new ConcurrentHashMap<>();
    //-------------------------------------------------

    public GameService(EventService eventService){
        this.eventService = eventService;
    }

    public void createGame(String id){
        Game game = new Game(new User(id, "User",  "×"));
        //System.out.println("this might be null, is it? "+id);
        activeGames.put(id, game);
    }

    public Game getGame(String id) {
        if(id == null) return null;
        return activeGames.get(id);
    }

    public void reset(Game game){
        game.reset();
        eventService.sendInitialState(game.getBoard(), game.users);
    }
    public boolean gameEnded(Game game) {
        return !(game.status == GameStatus.IN_PROGRESS);
    }

    public void makeMove(Game game) {
        boolean moved = game.makeMove();
        if (moved) {
            game.updateStatus();
            String move = game.getBoard()[game.lastMove];
            eventService.broadcastMove(game.lastMove, "<div class=\"xo\">" + move + "</div>", game.users);

            if(game.users.size()<2 && !gameEnded(game)) {
                Computer.makeMove(game);
                move = game.getBoard()[game.lastMove];
                eventService.broadcastMove(game.lastMove, "<div class=\"xo\">" + move + "</div>", game.users);
                game.updateStatus();
            }
            if (gameEnded(game)) {
                eventService.broadcastGameStatus(game.pOneScore, game.pTwoScore, game.users);
                if(!(game.status == GameStatus.DRAW)) {
                    eventService.broadcastWinner(game.getBoard()[game.winningLine[0]], game.winningLine, game.users);
                }
            }

        } else if (gameEnded(game)) {
            reset(game);
        }
    }

}