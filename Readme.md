# Multi-Tic-Tac

## Description
Multi-Tic-Tac is a simple online multiplayer X-O game built using Spring Boot and HTMX. The application allows multiple users to play Tic-Tac-Toe in real-time.

## Project Structure

~~~~multi-tic-tac/ 
├── .devcontainer/
├── .github/
├── .idea/
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   └── org/
        │       └── example/
        │           └── game/
        │               ├── GameApp.java
        │               ├── controller/
        │               │   └── GameController.java
        │               ├── model/
        │               │   ├── Board.java
        │               │   ├── Computer.java
        │               │   ├── Game.java
        │               │   ├── GameStatus.java
        │               │   └── User.java
        │               └── service/
        │                   └── GameService.java
        └── resources/
            ├── application.properties
            └── static/
                ├── index.html
                ├── sse.js
                ├── htmx.min.js
                └── style.css
~~~~


## Prerequisites
- Java 21
- Maven
- Docker (optional, for containerized development)

## Getting Started

### Building the Project
To build the project, run the following command:
```sh
mvn clean install
mvn clean package
```

### Running the Application
To run the application, use the following command:
```sh
mvn spring-boot:run
```

### Running Tests
To run the tests, use the following command:
```sh
mvn test
```

## Project Components
### Main Application
The main application class is GameApp:
```
@SpringBootApplication
public class GameApp {
    public static void main(String[] args) {
        SpringApplication.run(GameApp.class, args);
    }
}
```

### Controllers
The main controller for the game is GameController:
```
@Controller
public class GameController {
    // Handling connections and requests from UI
}
```

### Services
The game logic is handled by the GameService:
```
@Service
public class GameService {
    // Handling game logic
}
```
The UI updates triggered by Server Sent Events (SSE) in EventService:
```
@Service
public class EventService {
    // Brodcasting events to UI
}
```

### Configuration
The application configuration is located in `application.properties`.

## Frontend
The frontend of the application is built using HTML and CSS. The main HTML file is `index.html` and the CSS file is `style.css`.

### JavaScript Libraries
The project uses the following JavaScript libraries:

HTMX File: `htmx.min.js` used for making AJAX requests and handling real-time updates via SSE directly from HTML attributes.

SSE File: `sse.js` used for real-time updates from the server to the client.

### Development Environment
The project includes configuration files for various development environments:

- VS Code: .vscode
- IntelliJ IDEA: .idea
- Docker: .devcontainer

## License
This project is licensed under the MIT License.

## Contributing
Contributions are welcome! Please open an issue or submit a pull request.

