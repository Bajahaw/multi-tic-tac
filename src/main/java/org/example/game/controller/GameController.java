package org.example.game.controller;

import org.example.game.model.Computer;
import org.example.game.model.gameStatus;
import org.example.game.service.EventService;
import org.example.game.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

@Controller
public class GameController {
    //--------------------------------------------
    EventService eventService = new EventService();
    GameService gameService = new GameService();
    //--------------------------------------------
    @GetMapping("/connect")
    public ResponseEntity<SseEmitter> connect() {
        SseEmitter emitter = eventService.connect();
        eventService.sendInitialState(gameService.getGame().getBoard());
        return ResponseEntity.ok(emitter);
    }

    @PostMapping("/move/{cellIndex}")
    public ResponseEntity<String> makeMove(@PathVariable int cellIndex) {
        String move;
        if(gameService.makeMove(cellIndex)) {
            move = gameService.getGame().getBoard()[cellIndex];
            eventService.broadcastMove(cellIndex, "<div class=\"xo\">"+move+"</div>");

            if (gameService.gameEnded()){
                eventService.broadcastGameStatus(gameService.getGame().pOneScore, gameService.getGame().pTwoScore);
            }

        } else if (gameService.gameEnded()) {
            reset();
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reset")
    public ResponseEntity<String> reset() {
        gameService.reset();
        return ResponseEntity.ok().build();
    }
}
