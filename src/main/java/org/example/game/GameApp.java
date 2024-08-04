package org.example.game;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GameApp {
    public static void main(String[] args) {
        SpringApplication.run(GameApp.class, args);
    }
}
