package org.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MyController {

    @GetMapping("/update-content")
    @ResponseBody
    public String updateContent() {
        return """
                        <div class="xo">X</div>
                """;
    }
}
