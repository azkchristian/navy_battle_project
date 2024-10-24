package fr.lernejo.navy_battle;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public class GameRequestHandler implements HttpHandler {
    private static final ObjectMapper objectMapper = new ObjectMapper();

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
        Launcher.GameRequest gameRequest = objectMapper.readValue(requestBody, Launcher.GameRequest.class);
        String responseBody = objectMapper.writeValueAsString(gameRequest);
        exchange.sendResponseHeaders(202, responseBody.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBody.getBytes());
        }
    }
}
