package org.example.game.controller;

import jakarta.servlet.http.HttpSession;
import org.example.game.model.Game;
import org.example.game.model.User;
import org.example.game.service.EventService;
import org.example.game.service.GameService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


import java.io.IOException;
import java.util.List;
import java.util.Random;

@Controller
public class GameController {

    //--------------------------------------------
    private final EventService eventService;
    private final GameService gameService;
    //--------------------------------------------

    public GameController(EventService eventService, GameService gameService) {
        this.eventService = eventService;
        this.gameService = gameService;
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<String> handleException(Exception e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("An error occurred while processing the request" + e.getMessage());
    }

    @GetMapping("/")
    public String index(HttpSession session) {
        String clientId = (String) session.getAttribute("clientId");
        if (clientId == null) {

            clientId = "" + Random
                    .from(new Random(System.currentTimeMillis()))
                    .nextInt(10000, 100000);

            session.setAttribute("clientId", clientId);
        }
        return "index.html";
    }

    @GetMapping("/connect")
    public ResponseEntity<SseEmitter> connect(HttpSession session) {
        String clientId = (String) session.getAttribute("clientId");
        if (clientId == null) {
            return null;
        }
        User user = gameService.getUser(clientId);
        if (user == null) {
            user = gameService.createGame(clientId).users.getFirst();
        }

        SseEmitter emitter = eventService.connect(clientId);
        eventService.sendEvent(clientId, "clientId", clientId);
        eventService.sendEvent(clientId, "player1name", user.getName());
        eventService.sendInitialState(user.getGame().getBoard(), List.of(user));


        if (!user.isFree()) {
            String button = """
                <button class="btn red"
                    hx-get="/leave"
                    hx-trigger="click"
                    hx-target=".state">
                    leave game
                </button>""";
            eventService.sendEvent(clientId, "state", button);
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(emitter);
    }

    @PostMapping("/move/{cellIndex}")
    public ResponseEntity<String> makeMove(@PathVariable int cellIndex, HttpSession session) {
        String clientId = (String) session.getAttribute("clientId");
        User curUuser = gameService.getUser(clientId);
        if (curUuser == null || !eventService.isClientConnected(clientId)) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }
        Game game = curUuser.getGame();
        game.users.forEach(user -> {
            if (user.getId().equals(clientId)) {
                user.updateMove(cellIndex);
                gameService.makeMove(game);
            }
        });
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @GetMapping("/random")
    public ResponseEntity<String> random(HttpSession session) {
        String clientId = (String) session.getAttribute("clientId");
        User curUser = gameService.getUser(clientId);
        if (curUser == null || !eventService.isClientConnected(clientId)) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Client not connected.");
        }

        User opponent = gameService.getRandmoUser(curUser.getId());
        if(opponent == null) {
            eventService.sendEvent(clientId, "state", "Seems that no opponents online!");
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
        GameService.logger.info("opponent: {}", opponent.getName());

        Game game = opponent.getGame();
        game.setUserOnHold(curUser);
        String btnAccept = "<button class=\"btn green\" hx-get=\"/accept\" hx-trigger=\"click\" hx-target=\".state\">Join "+curUser.getName()+"</button>";
        eventService.sendEvent(clientId, "state", "Invitation sent ...");
        eventService.sendEvent(opponent.getId(), "btnleave", btnAccept);

        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    @PostMapping("/join")
    public ResponseEntity<String> join(HttpSession session, @RequestParam String gameId) {

        String clientId = (String) session.getAttribute("clientId");
        User curUser = gameService.getUser(clientId);
        if (curUser == null || !eventService.isClientConnected(clientId)) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Client not connected.");
        }

        User user = gameService.getUser(gameId);
        if (user == null || !eventService.isClientConnected(gameId)) {
            eventService.sendEvent(clientId, "state", "game not found or user not connected");
            return ResponseEntity
                    .badRequest()
                    .body("game not found or user not connected");
        }

        if (!user.isFree() || user == curUser) {
            eventService.sendEvent(clientId, "state", "Game already has two players.");
            return ResponseEntity
                    .badRequest()
                    .body("Game already has two players.");
        }

        Game game = user.getGame();
        game.setUserOnHold(curUser);

        //--------- Join request

        String btnAccept = "<button class=\"btn green\" hx-get=\"/accept\" hx-trigger=\"click\" hx-target=\".state\">Join "+curUser.getName()+"</button>";
        eventService.sendEvent(clientId, "state", "Invitation sent ...");
        eventService.sendEvent(gameId, "btnleave", btnAccept);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body("Invite");
    }

    @GetMapping("/accept")
    public ResponseEntity<String> accept(HttpSession session) {
        String clientId = (String) session.getAttribute("clientId");
        User curUser = gameService.getUser(clientId);
        if (curUser == null || !eventService.isClientConnected(clientId)) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Client not connected.");
        }

        Game game = curUser.getGame();
        game.setPlayerTwo(game.getUserOnHold());

        String btnLeave = "<button class=\"btn red\" hx-get=\"/leave\" hx-trigger=\"click\" hx-target=\".state\">leave game</button>";
        eventService.sendEvent(game.users.getFirst().getId(), "btnleave", btnLeave);
        eventService.sendEvent(game.users.getLast().getId(), "btnleave", btnLeave);

        eventService.sendInitialState(game.getBoard(), game.users);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @GetMapping("/leave")
    public ResponseEntity<String> leave(HttpSession session) {

        String clientId = (String) session.getAttribute("clientId");
        if (gameService.getUser(clientId) == null || !eventService.isClientConnected(clientId)) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Connection error.");
        }

        Game game = gameService.getUser(clientId).getGame();

        User user1 = game.users.getFirst();
        User user2 = game.users.getLast();

        gameService.getGame(user2.getId()).reset();
        user2.joinGame(gameService.getGame(user2.getId()));
        game.setPlayerTwo(user1);

        String btnRandom = """
                <button class="btn"
                        hx-get="/random"
                        hx-trigger="click"
                        hx-target=".state">
                        Random
                </button>""";
        eventService.sendEvent(clientId, "state", "you left the game");
        eventService.sendEvent(clientId.equals(user1.getId()) ? user2.getId() : user1.getId(), "state", clientId + " left the game");
        eventService.sendEvent(user1.getId(), "btnleave", btnRandom);
        eventService.sendEvent(user2.getId(), "btnleave", btnRandom);

        eventService.sendInitialState(user1.getGame().getBoard(), user1.getGame().users);
        eventService.sendInitialState(user2.getGame().getBoard(), user2.getGame().users);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @PostMapping("/rename")
    public ResponseEntity<String> rename(@RequestParam String newName, HttpSession session) {
        String clientId = (String) session.getAttribute("clientId");
        User curUser = gameService.getUser(clientId);

        if (curUser == null)
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Client not connected.");

        curUser.setName(newName);
        if(eventService.isClientConnected(clientId)){
            eventService.sendEvent(curUser.getId(), "player1name", newName);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body("username: " + newName);
        }
        return ResponseEntity
                .status(HttpStatus.PARTIAL_CONTENT)
                .body("username: " + newName);
    }
}