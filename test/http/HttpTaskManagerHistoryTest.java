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

class HttpTaskManagerHistoryTest {
    private HttpTaskServer server;
    private TaskManager mgr;
    private Gson gson;
    private HttpClient client;
    private static final String URL = "http://localhost:8080/history";

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
    void getHistoryTracksViews() throws Exception {
        Task task1 = new Task("Task 1", "Description", LocalDateTime.now(), Duration.ofMinutes(5));
        Task task2 = new Task("Task 2", "Description", LocalDateTime.now().plusMinutes(6),
                Duration.ofMinutes(5));

        client.send(HttpRequest.newBuilder(URI.create("http://localhost:8080/tasks"))
                        .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task1))).build(),
                HttpResponse.BodyHandlers.ofString());
        client.send(HttpRequest.newBuilder(URI.create("http://localhost:8080/tasks"))
                        .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task2))).build(),
                HttpResponse.BodyHandlers.ofString());

        HttpResponse<String> getResp = client.send(
                HttpRequest.newBuilder(URI.create("http://localhost:8080/tasks")).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        Task[] tasks = gson.fromJson(getResp.body(), Task[].class);
        int id1 = tasks[0].getId();
        int id2 = tasks[1].getId();

        client.send(HttpRequest.newBuilder(URI.create("http://localhost:8080/tasks/" + id1))
                .GET().build(), HttpResponse.BodyHandlers.ofString());
        client.send(HttpRequest.newBuilder(URI.create("http://localhost:8080/tasks/" + id2))
                .GET().build(), HttpResponse.BodyHandlers.ofString());

        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder(URI.create(URL)).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, resp.statusCode());
        Task[] hist = gson.fromJson(resp.body(), Task[].class);
        assertEquals(2, hist.length);
    }
}
