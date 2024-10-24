package fr.lernejo.navy_battle;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class FireRequestHandler implements HttpHandler {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Map<String, String> gameBoard = Launcher.getGameBoard();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            handleFire(exchange);
        } else {
            exchange.sendResponseHeaders(404, -1);
        }
    }

    private void handleFire(HttpExchange exchange) throws IOException {
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
