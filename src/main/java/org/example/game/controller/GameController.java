package org.example.game.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.game.model.GameState;
import org.example.game.service.GameService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class GameController {
    GameService gameService = new GameService();
    @GetMapping("/update-content")
    @ResponseBody
    public String updateContent(HttpServletRequest request) {
        String triggerId = request.getHeader("HX-Trigger");
        return gameService.makeMove(Integer.parseInt(triggerId)).toHtml();
    }
}
