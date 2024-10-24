package fr.lernejo.navy_battle;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class Launcher {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, String> gameBoard = new HashMap<>();

    public static void main(String[] args) throws IOException {
        new Launcher().startGame(args);
    }

    private void startGame(String[] args) throws IOException {
        int port = Integer.parseInt(args[0]);
        String adversaryUrl = args.length > 1 ? args[1] : null;

        initializeGameBoard();

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/ping", this::handlePing);
        server.createContext("/api/game/start", new GameRequestHandler(this));
        server.createContext("/api/game/fire", new FireHandler(this));

        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port: " + port);

        if (adversaryUrl != null) {
            sendStartGameRequest(adversaryUrl, port);
        }
    }

    private void initializeGameBoard() {
        gameBoard.put("B2", "hit");
        gameBoard.put("C3", "sunk");
    }

    private void handlePing(HttpExchange exchange) throws IOException {
        String body = "OK";
        exchange.sendResponseHeaders(200, body.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body.getBytes());
        }
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public Map<String, String> getGameBoard() {
        return gameBoard;
    }

    public String processFire(String cell) {
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

    public boolean checkIfShipLeft() {
        return !gameBoard.isEmpty();
    }

    private void sendStartGameRequest(String adversaryUrl, int myPort) {
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
}
