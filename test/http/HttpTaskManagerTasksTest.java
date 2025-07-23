package http;

import com.google.gson.Gson;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.*;
import task.Task;
import task.Status;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskManagerTasksTest {
    private HttpTaskServer server;
    private TaskManager manager;
    private Gson gson;
    private HttpClient client;
    private static final String BASE = "http://localhost:8080/tasks";

    @BeforeEach
    void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        server = new HttpTaskServer(manager);
        gson = HttpTaskServer.getGson();
        client = HttpClient.newHttpClient();
        server.start();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void postTasksCreates() throws Exception {
        Task task = new Task("Task", "Description", LocalDateTime.now(), Duration.ofMinutes(5));
        String body = gson.toJson(task);
        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder(URI.create(BASE))
                        .POST(HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(201, resp.statusCode());
        assertEquals(1, manager.getTasks().size());
    }

    @Test
    void getTasksLists() throws Exception {
        Task task = new Task("Task", "Description", LocalDateTime.now(), Duration.ofMinutes(1));
        String body = gson.toJson(task);
        client.send(HttpRequest.newBuilder(URI.create(BASE))
                        .POST(HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString());

        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder(URI.create(BASE)).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, resp.statusCode());
        Task[] arr = gson.fromJson(resp.body(), Task[].class);
        assertEquals(1, arr.length);
    }

    @Test
    void getTaskById200() throws Exception {
        Task task = new Task("Task", "Description", LocalDateTime.now(), Duration.ofMinutes(2));
        String body = gson.toJson(task);
        client.send(HttpRequest.newBuilder(URI.create(BASE))
                        .POST(HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString());

        int taskId = manager.getTasks().getFirst().getId();

        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder(URI.create(BASE + "/" + taskId)).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, resp.statusCode());
        Task fetched = gson.fromJson(resp.body(), Task.class);
        assertEquals(taskId, fetched.getId());
    }

    @Test
    void getTaskById404() throws Exception {
        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder(URI.create(BASE + "/999")).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(404, resp.statusCode());
    }

    @Test
    void postTasksUpdate() throws Exception {
        Task task = new Task("Task", "Description", LocalDateTime.now(), Duration.ofMinutes(3));
        String body = gson.toJson(task);
        client.send(HttpRequest.newBuilder(URI.create(BASE))
                        .POST(HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString());

        Task created = manager.getTasks().getFirst();
        created.setStatus(Status.DONE);
        String upd = gson.toJson(created);
        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder(URI.create(BASE))
                        .POST(HttpRequest.BodyPublishers.ofString(upd)).build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(201, resp.statusCode());
        assertEquals(Status.DONE, manager.getTaskById(created.getId()).getStatus());
    }

    @Test
    void deleteTaskById() throws Exception {
        Task task = new Task("Task", "Description", LocalDateTime.now(), Duration.ofMinutes(4));
        String body = gson.toJson(task);
        client.send(HttpRequest.newBuilder(URI.create(BASE))
                        .POST(HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString());

        int id = manager.getTasks().getFirst().getId();

        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder(URI.create(BASE + "/" + id))
                        .DELETE().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, resp.statusCode());
        assertNull(manager.getTaskById(id));
    }
}