package fr.lernejo.navy_battle;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class FireHandler implements HttpHandler {
    private final Launcher launcher;

    public FireHandler(Launcher launcher) {
        this.launcher = launcher;
    }

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

        String consequence = launcher.processFire(cell);
        boolean shipLeft = launcher.checkIfShipLeft();

        response.put("consequence", consequence);
        response.put("shipLeft", shipLeft);

        String jsonResponse = launcher.getObjectMapper().writeValueAsString(response);
        exchange.sendResponseHeaders(200, jsonResponse.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(jsonResponse.getBytes());
        }
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
