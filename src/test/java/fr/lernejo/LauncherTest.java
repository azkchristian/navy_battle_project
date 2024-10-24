package fr.lernejo.navy_battle;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LauncherTest {

    @Test
    void testGameRequest() {
        Launcher.GameRequest request = new Launcher.GameRequest();
        request.id = "1";
        request.url = "http://localhost:8080";
        request.message = "hello";

        assertEquals("1", request.id);
        assertEquals("http://localhost:8080", request.url);
        assertEquals("hello", request.message);
    }

    // Additional tests can be added here
}
