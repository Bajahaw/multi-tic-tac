package org.example.game.controller;

import org.example.game.service.EventService;
import org.example.game.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Controller
public class GameController {

    //--------------------------------------------
    EventService eventService = new EventService();
    GameService gameService = new GameService(eventService);
    //--------------------------------------------

    @GetMapping("/connect")
    public SseEmitter connect() {
        SseEmitter emitter = eventService.connect();
        new Thread(() -> {
            eventService.sendInitialState(gameService.getGame().getBoard());
        });
        return emitter;
    }

    @PostMapping("/move/{cellIndex}")
    public ResponseEntity<String> makeMove(@PathVariable int cellIndex) {
        gameService.getGame().playerOne.updateMove(cellIndex);
        gameService.makeMove();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reset")
    public ResponseEntity<String> reset() {
        gameService.reset();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reload")
    public ResponseEntity<String> reload() {
        eventService.reloadSse();
        return ResponseEntity.noContent().build();
    }
}
