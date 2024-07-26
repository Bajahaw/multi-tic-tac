package org.example.game.controller;

import jakarta.servlet.http.HttpSession;
import org.example.game.model.Game;
import org.example.game.model.User;
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
import java.util.UUID;

@Controller
public class GameController {

    //--------------------------------------------
    EventService eventService = new EventService();
    GameService gameService = new GameService(eventService);
    //--------------------------------------------

    @GetMapping("/connect")
    public SseEmitter connect(HttpSession session) {
        System.out.println("connecting .. ");
        String clientId = (String) session.getAttribute("clientId");
        if (clientId == null) {
            session.setAttribute("clientId",id(session).getBody());
        }

        Game game = gameService.getGame(clientId);
        if (game == null) {
            gameService.createGame(clientId);
        }

        SseEmitter emitter = eventService.connect(clientId);
        Game finalGame = gameService.getGame(clientId);
        new Thread(() -> {
            eventService.sendInitialState(finalGame.getBoard(), finalGame.users);
        });
        return emitter;
    }

    @GetMapping("/id")
    public ResponseEntity<String> id(HttpSession session) {
        System.out.println("getting id");
        String clientId = (String) session.getAttribute("clientId");
        if (clientId == null) {
            System.out.println("getting session id: " + clientId);
            clientId = ""+ Random.from(new Random(System.currentTimeMillis())).nextInt(10000, 100000);
        }
        session.setAttribute("clientId", clientId);
        return ResponseEntity.ok(clientId);
    }

    @PostMapping("/move/{cellIndex}")
    public ResponseEntity<String> makeMove(@PathVariable int cellIndex, HttpSession session) {
        String clientId = (String) session.getAttribute("clientId");
        if (clientId == null) {return ResponseEntity.notFound().build();}
        Game game = gameService.getGame(clientId);
        if (game != null ) game = game.users.getFirst().getGame();
        System.out.println(game.users.getFirst().getId() +" <-> " + game.users.getLast().getId() + " : " + clientId);
        Game finalGame = game;
        game.users.forEach(user -> {
            System.out.println("user id: " + user.getId());
            System.out.println("client id: " + clientId);
            if(user.getId() == clientId) {
                System.out.println("made move: " + user.getId());
                user.updateMove(cellIndex);
                gameService.makeMove(finalGame);
            }
        });
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reset")
    public ResponseEntity<String> reset(HttpSession session) {
        String clientId = (String) session.getAttribute("clientId");
        if (clientId == null) {return ResponseEntity.notFound().build();}
        Game game = gameService.getGame(clientId);
        gameService.reset(game);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/reload")
    public ResponseEntity<String> reload(HttpSession session) {
        String clientId = (String) session.getAttribute("clientId");
        if (clientId == null) {return ResponseEntity.noContent().build();}
        Game game = gameService.getGame(clientId);
        if (game != null)game = game.users.getFirst().getGame();
        if (game == null) {return ResponseEntity.noContent().build();}
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
        eventService.notifyPlayer(gameId, clientId);
        return ResponseEntity.noContent().build();
    }
}
