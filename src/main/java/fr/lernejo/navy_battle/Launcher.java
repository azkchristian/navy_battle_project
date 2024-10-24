package fr.lernejo.navy_battle;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class Launcher {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static Map<String, String> gameBoard = new HashMap<>();

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(args[0]);
        String adversaryUrl = args.length > 1 ? args[1] : null;

        gameBoard.put("B2", "hit");
        gameBoard.put("C3", "sunk");

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/ping", exchange -> {
            String body = "OK";
            exchange.sendResponseHeaders(200, body.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body.getBytes());
            }
        });

        server.createContext("/api/game/start", new StartGameHandler());
        server.createContext("/api/game/fire", new FireHandler());

        server.setExecutor(null);
        server.start();

        if (adversaryUrl != null) {
            sendStartGameRequest(adversaryUrl, port);
        }
    }

    static class StartGameHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String requestBody = new String(exchange.getRequestBody().readAllBytes());
                GameRequest gameRequest = objectMapper.readValue(requestBody, GameRequest.class);
                String responseBody = objectMapper.writeValueAsString(gameRequest);
                exchange.sendResponseHeaders(202, responseBody.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBody.getBytes());
                }
            } else {
                exchange.sendResponseHeaders(404, -1);
            }
        }
    }

    static class FireHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                String cell = query != null ? getQueryParam(query, "cell") : null;

                Map<String, Object> response = new HashMap<>();

                if (cell == null || !isValidCell(cell)) {
                    exchange.sendResponseHeaders(400, -1);
                    return;
                }

                String consequence = processFire(cell);
                boolean shipLeft = checkIfShipLeft();

                response.put("consequence", consequence);
                response.put("shipLeft", shipLeft);

                String jsonResponse = objectMapper.writeValueAsString(response);
                exchange.sendResponseHeaders(200, jsonResponse.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(jsonResponse.getBytes());
                }
            } else {
                exchange.sendResponseHeaders(404, -1);
            }
        }

        private String processFire(String cell) {
            if (gameBoard.containsKey(cell)) {
                String result = gameBoard.get(cell);
                if (result.equals("sunk")) {
                    gameBoard.remove(cell);
                    return "sunk";
                }
                return "hit";
            }
            return "miss";
        }

        private boolean checkIfShipLeft() {
            return !gameBoard.isEmpty();
        }

        private String getQueryParam(String query, String param) {
            for (String pair : query.split("&")) {
                String[] keyValue = pair.split("=");
                if (keyValue[0].equals(param)) {
                    return keyValue.length > 1 ? keyValue[1] : null;
                }
            }
            return null;
        }

        private boolean isValidCell(String cell) {
            return cell.matches("^[A-J][1-9]|10$");
        }
    }

    private static void sendStartGameRequest(String adversaryUrl, int myPort) {
        try {
            var client = java.net.http.HttpClient.newHttpClient();
            String requestBody = String.format("{\"id\":\"1\", \"url\":\"http://localhost:%d\", \"message\":\"hello\"}", myPort);
            var request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(adversaryUrl + "/api/game/start"))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

            var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            System.out.println("Response from start game request: " + response.body());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static class GameRequest {
        public String id;
        public String url;
        public String message;
    }
}
