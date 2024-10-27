package org.example.game.controller;

import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.Logger;
import org.example.game.model.Game;
import org.example.game.model.User;
import org.example.game.service.EventService;
import org.example.game.service.GameService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


import java.util.List;
import org.apache.commons.text.StringEscapeUtils;
import java.util.Random;

@Controller
public class GameController {

    //--------------------------------------------
    private final EventService eventService;
    private final GameService gameService;
    private final Logger log = org.apache.logging.log4j.LogManager.getLogger(GameController.class);
    //--------------------------------------------

    public GameController(EventService eventService, GameService gameService) {
        this.eventService = eventService;
        this.gameService = gameService;
    }

    /**
     * This method handles global exceptions from being thrown by the application.
     * @param e the exception
     * @return a response entity with a status of BAD_REQUEST and a message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        log.error("An error occurred while processing the request: ", e);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("An error occurred while processing the request" + e.getMessage());
    }

    /**
     * index method is called first to create a random clientId for each user. and to
     * assign a unique JSESSIONID before it is used connect method.
     * 
     * @param session the HttpSession object
     * @return the index page
     */
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

    /**
     * connect method is called after the index method to make an SSE connection
     * with the client and send the clientId, the name, and the initial state
     * - whether it is new game or existing one - to the client. Also, if the user
     * is not in his initial game, it will send a html button to leave the game.
     *
     * @param session the HttpSession object
     * @return a response entity with a status of OK and an SseEmitter object
     */
    @GetMapping("/connect")
    public ResponseEntity<SseEmitter> connect(HttpSession session) {
        String clientId = (String) session.getAttribute("clientId");
        if (clientId == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }
        User user = gameService.getUser(clientId);
        if (user == null) {
            user = gameService.createGame(clientId).users.getFirst();
        }

        SseEmitter emitter = eventService.connect(clientId);
        eventService.sendEvent(clientId, "clientId", clientId);
        eventService.sendEvent(clientId, "player1name", user.getName());
        eventService.sendInitialState(user.getGame().getBoard(), List.of(user));

        // todo: implement all html using template engine
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

    /**
     * makeMove method is called when a user makes a move. It updates the current
     * user's move after validating it in User class, and then calls the makeMove
     * method from the GameService class.
     *
     * @param cellIndex the index of the cell
     * @param session the HttpSession object
     * @return a response entity with a status of NO_CONTENT, or BAD_REQUEST if the
     * client is not connected
     */
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

    /**
     * random method is called when a user clicks on the random button to find an
     * opponent. It sends a message to the user whether opponents are found or not, and
     * sends an invitation to the opponent if found.
     *
     * @param session the HttpSession object
     * @return a response entity with a status of OK if an opponent is found, or
     * NOT_FOUND if no opponents are found, or BAD_REQUEST if the client is not connected
     */
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

        Game game = opponent.getGame();
        game.setUserOnHold(curUser);
        String btnAccept = "<button class=\"btn green\" hx-get=\"/accept\" hx-trigger=\"click\" hx-target=\".state\">Join "+curUser.getName()+"</button>";
        eventService.sendEvent(clientId, "state", "Invitation sent ...");
        eventService.sendEvent(opponent.getId(), "btnleave", btnAccept);

        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    /**
     * join (invite -as should be) method is called when a user send a request to other user
     * to join. It sends a message to the user whether the game is found or not, and sends an
     * invitation with the join button to the opponent if found.
     *
     * @param session the HttpSession object
     * @param userId the id of the game or user to invite
     * @return a response entity with a status of OK if the game is found, or
     * BAD_REQUEST otherwise.
     */
    @PostMapping("/invite")
    public ResponseEntity<String> invite(HttpSession session, @RequestParam String userId) {

        String clientId = (String) session.getAttribute("clientId");
        User curUser = gameService.getUser(clientId);
        if (curUser == null || !eventService.isClientConnected(clientId)) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Client not connected.");
        }

        User user = gameService.getUser(userId);
        if (user == null || !eventService.isClientConnected(userId)) {
            eventService.sendEvent(clientId, "state", "game not found or user not connected");
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("game not found or user not connected");
        }

        if (!user.isFree() || user == curUser) {
            eventService.sendEvent(clientId, "state", "Game already has two players.");
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Game already has two players.");
        }

        Game game = user.getGame();
        game.setUserOnHold(curUser);

        //--------- Join request

        String btnAccept = "<button class=\"btn green\" hx-get=\"/accept\" hx-trigger=\"click\" hx-target=\".state\">Join "+curUser.getName()+"</button>";
        eventService.sendEvent(clientId, "state", "Invitation sent ...");
        eventService.sendEvent(userId, "btnleave", btnAccept);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body("Invite");
    }

    /**
     * accept method is called when a user accepts an invitation to join a game.
     * It adds the user to the game and sends the initial state of the game to both players.
     * It also sends a leave button to the players.
     *
     * @param session the HttpSession object
     * @return a response entity with a status of NO_CONTENT, or BAD_REQUEST if the
     * client is not connected
     */
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

        String btnLeave = """
                <button class="btn red"
                     hx-get="/leave"
                     hx-trigger="click"
                     hx-target=".state">
                     leave game
                 </button>""";
        eventService.sendEvent(game.users.getFirst().getId(), "btnleave", btnLeave);
        eventService.sendEvent(game.users.getLast().getId(), "btnleave", btnLeave);

        eventService.sendInitialState(game.getBoard(), game.users);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    /**
     * leave method is called when a user leaves a game. Each user returns to their initial
     * game. and it sends a message to the players that the user has left the game, and sends a random button to the
     * players to find a new opponent.
     *
     * @param session the HttpSession object
     * @return a response entity with a status of NO_CONTENT, or BAD_REQUEST if the
     * client is not connected
     */
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

    /**
     * rename method is called when a user changes their name. It sends the new name to the
     * players in the game.
     *
     * @param newName the new name
     * @param session the HttpSession object
     * @return a response entity with a status of OK, or PARTIAL_CONTENT if the
     * client is not connected
     */
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
                    .body("username: " + StringEscapeUtils.escapeHtml4(newName));
        }
        return ResponseEntity
                .status(HttpStatus.PARTIAL_CONTENT)
                .body("username: " + StringEscapeUtils.escapeHtml4(newName));
    }
}