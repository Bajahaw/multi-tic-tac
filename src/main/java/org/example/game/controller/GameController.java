package org.example.game.controller;

import jakarta.servlet.http.HttpSession;
import org.example.game.model.Game;
import org.example.game.service.EventService;
import org.example.game.service.GameService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Random;

@Controller
public class GameController {

    //--------------------------------------------
    EventService eventService = new EventService();
    GameService gameService = new GameService(eventService);
    //--------------------------------------------

    @GetMapping("/connect")
    public SseEmitter connect(HttpSession session) {
        //System.out.println("Connecting");
        String clientId = (String) session.getAttribute("clientId");
        if (clientId == null) {
            clientId = "" + Random.from(new Random(System.currentTimeMillis())).nextInt(10000, 100000);
            session.setAttribute("clientId", clientId);
        }

        Game game = gameService.getGame(clientId);
        if (game == null) {
            gameService.createGame(clientId);
            game = gameService.getGame(clientId);
        }
        //System.out.println("connecting to .. " + clientId);
        SseEmitter emitter = eventService.connect(clientId);
        eventService.sendEvent(clientId, "clientId", clientId);
        eventService.sendEvent(clientId, "player1name", "player"+clientId);
        eventService.sendInitialState(game.getBoard(), game.users);
        return emitter;
    }

    @PostMapping("/move/{cellIndex}")
    public ResponseEntity<String> makeMove(@PathVariable int cellIndex, HttpSession session) {
        String clientId = (String) session.getAttribute("clientId");
        if (clientId == null) {
            return ResponseEntity.notFound().build();
        }
        Game game = gameService.getGame(clientId).users.getFirst().getGame();
        ///System.out.println(game.users.getFirst().getId() + " <-> " + game.users.getLast().getId() + " : " + clientId);
        game.users.forEach(user -> {
            ///System.out.println("user id: " + user.getId());
            ///System.out.println("client id: " + clientId);
            if (user.getId().equals(clientId)) {
                //System.out.println("made move: " + user.getId());
                user.updateMove(cellIndex);
                gameService.makeMove(game);
            }
        });
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reset")
    public ResponseEntity<String> reset(HttpSession session) {
        String clientId = (String) session.getAttribute("clientId");
        if (clientId == null) {
            return ResponseEntity.notFound().build();
        }
        Game game = gameService.getGame(clientId);
        gameService.reset(game);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/reload")
    public ResponseEntity<String> reload(HttpSession session) {
        String clientId = (String) session.getAttribute("clientId");
        if (clientId == null) {
            return ResponseEntity.noContent().build();
        }
        Game game = gameService.getGame(clientId).users.getFirst().getGame();;
        if (game == null) {
            return ResponseEntity.noContent().build();
        }
        eventService.sendInitialState(game.getBoard(), game.users);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/join")
    public ResponseEntity<String> join(HttpSession session, @RequestParam String gameId) { // Changed to RequestParam
        String clientId = (String) session.getAttribute("clientId");

        if (clientId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Client not logged in.");
        }

        Game game = gameService.getGame(gameId);
        if (game == null) {
            System.out.println("game not found");
            return ResponseEntity.notFound().build();
        }
        System.out.println("got game: " + gameId);
        Game clientGame = gameService.getGame(clientId);
        if (clientGame == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Client game not found.");
        }
        System.out.println("got game: " + clientId);
        if (clientGame.users.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Client users not available.");
        }

        game.setPlayerTwo(clientGame.users.getFirst());
        eventService.sendEvent(gameId, "player2name", "player"+clientId);
        eventService.sendInitialState(game.getBoard(), game.users);
        return ResponseEntity.ok("joined game: "    +gameId);
    }
}
