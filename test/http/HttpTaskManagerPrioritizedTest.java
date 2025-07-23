package http;

import com.google.gson.Gson;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.*;
import task.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskManagerPrioritizedTest {
    private HttpTaskServer server;
    private TaskManager mgr;
    private Gson gson;
    private HttpClient client;
    private static final String URL = "http://localhost:8080/prioritized";

    @BeforeEach
    void setUp() throws IOException {
        mgr = new InMemoryTaskManager();
        server = new HttpTaskServer(mgr);
        gson = HttpTaskServer.getGson();
        client = HttpClient.newHttpClient();
        server.start();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void prioritizedOrder() throws Exception {
        LocalDateTime base = LocalDateTime.of(2025, 1, 1, 10, 0);
        Task task1 = new Task("Task 1", "Description", base, Duration.ofMinutes(5));
        Task task2 = new Task("Task 2", "Description", base.minusHours(2), Duration.ofMinutes(5));

        client.send(HttpRequest.newBuilder(URI.create("http://localhost:8080/tasks"))
                        .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task1))).build(),
                HttpResponse.BodyHandlers.ofString());

        client.send(HttpRequest.newBuilder(URI.create("http://localhost:8080/tasks"))
                        .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task2))).build(),
                HttpResponse.BodyHandlers.ofString());

        var resp = client.send(HttpRequest.newBuilder(URI.create(URL)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        Task[] arr = gson.fromJson(resp.body(), Task[].class);
        assertEquals(2, arr.length);
        assertTrue(arr[0].getStartTime().isBefore(arr[1].getStartTime()));
    }
}
