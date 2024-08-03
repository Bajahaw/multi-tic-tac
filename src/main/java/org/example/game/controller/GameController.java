package org.example.game.controller;

import jakarta.servlet.http.HttpSession;
import org.example.game.model.Game;
import org.example.game.model.User;
import org.example.game.service.EventService;
import org.example.game.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final EventService eventService;
    private final GameService gameService;
    //--------------------------------------------

    @Autowired
    public GameController(EventService eventService, GameService gameService) {
        this.eventService = eventService;
        this.gameService = gameService;
    }

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
        game = game.users.getFirst().getGame();

        //System.out.println("connecting to .. " + clientId);
        SseEmitter emitter = eventService.connect(clientId);
        eventService.sendEvent(clientId, "clientId", clientId);
        eventService.sendEvent(clientId, "player1name", "player" + clientId);
        eventService.sendInitialState(game.getBoard(), game.users);
        return emitter;
    }

    @PostMapping("/move/{cellIndex}")
    public ResponseEntity<String> makeMove(@PathVariable int cellIndex, HttpSession session) {
        String clientId = (String) session.getAttribute("clientId");
        if (clientId == null || !eventService.isClientConnected(clientId)) {
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

    @GetMapping("/reload")
    public ResponseEntity<String> reload(HttpSession session) {
        String clientId = (String) session.getAttribute("clientId");
        if (clientId == null || !eventService.isClientConnected(clientId)) {
            return ResponseEntity.noContent().build();
        }
        Game game = gameService.getGame(clientId).users.getFirst().getGame();
        if (game == null) {
            return ResponseEntity.noContent().build();
        }
        String button = "<button class=\"btn red\" hx-get=\"/leave\" hx-trigger=\"click\" hx-target=\".state\">leave game</button>";
        if (game.users.size() > 1) {
            eventService.sendEvent(clientId, "state", button);
            String id = game.users.getFirst().getId();
            eventService.sendEvent(clientId, "player2name", "player" + (id.equals(clientId) ? game.users.getLast().getId() : id));
            eventService.broadcastGameStatus(game.pOneScore, game.pTwoScore, game.users);
        }
        eventService.sendInitialState(game.getBoard(), game.users.stream().filter(user -> user.getId().equals(clientId)).toList());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/join")
    public ResponseEntity<String> join(HttpSession session, @RequestParam String gameId) { // Changed to RequestParam
        String clientId = (String) session.getAttribute("clientId");

        if (clientId == null || !eventService.isClientConnected(clientId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Client not connected.");
        }

        Game game = gameService.getGame(gameId);
        if (game == null || !eventService.isClientConnected(gameId)) {
            eventService.sendEvent(clientId, "state", "game not found or user not connected");
            return ResponseEntity.badRequest().body("game not found or user not connected");
        }
        System.out.println("got game: " + gameId);
        System.out.println("got game: " + clientId);
        if (game.users.size() > 1) {
            eventService.sendEvent(clientId, "state", "Game already has two players.");
            return ResponseEntity.badRequest().body("Game already has two players.");
        }

        game.setPlayerTwo(gameService.getGame(clientId).users.getFirst());
        //eventService.sendEvent(clientId, "player2name", "player"+gameId);
        //eventService.sendEvent(gameId, "player2name", "player"+clientId);

        String button = "<button class=\"btn red\" hx-get=\"/leave\" hx-trigger=\"click\" hx-target=\".state\">leave game</button>";
        eventService.sendEvent(clientId, "btnleave", button);
        eventService.sendEvent(gameId, "btnleave", button);

        eventService.sendInitialState(game.getBoard(), game.users);

        return ResponseEntity.ok("Join");
    }

    @GetMapping("/leave")
    public ResponseEntity<String> leave(HttpSession session) {
        String clientId = (String) session.getAttribute("clientId");
        if (clientId == null || !eventService.isClientConnected(clientId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Connection error.");
        }
        Game game = gameService.getGame(clientId);
        if (game == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Game not connected.");
        }
        game = game.users.getFirst().getGame();

        User user1 = game.users.getFirst();
        User user2 = game.users.getLast();

        gameService.getGame(user2.getId()).reset();
        user2.joinGame(gameService.getGame(user2.getId()));
        game.setPlayerTwo(user1);

        eventService.sendEvent(clientId, "state", "left the game");
        eventService.sendEvent(clientId.equals(user1.getId()) ? user2.getId() : user1.getId(), "state", clientId + " left the game");
        eventService.sendEvent(user1.getId(), "btnleave", "");
        eventService.sendEvent(user2.getId(), "btnleave", "");

        eventService.sendInitialState(user1.getGame().getBoard(), user1.getGame().users);
        eventService.sendInitialState(user2.getGame().getBoard(), user2.getGame().users);
        return ResponseEntity.noContent().build();
    }
}
