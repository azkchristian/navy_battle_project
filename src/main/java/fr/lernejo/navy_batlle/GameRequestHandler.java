package fr.lernejo.navy_battle;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public class GameRequestHandler implements HttpHandler {
    private final Launcher launcher;

    public GameRequestHandler(Launcher launcher) {
        this.launcher = launcher;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            handleStartGame(exchange);
        } else {
            exchange.sendResponseHeaders(404, -1);
        }
    }

    private void handleStartGame(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes());

        try {
            GameRequest gameRequest = launcher.getObjectMapper().readValue(requestBody, GameRequest.class);
            String responseBody = launcher.getObjectMapper().writeValueAsString(gameRequest);

            exchange.sendResponseHeaders(202, responseBody.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBody.getBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
            exchange.sendResponseHeaders(400, -1); // Bad request
        }
    }
}
