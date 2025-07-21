package http;

import com.google.gson.Gson;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.*;
import task.Epic;
import task.Subtask;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskManagerSubtasksTest {
    private HttpTaskServer server;
    private TaskManager mgr;
    private Gson gson;
    private HttpClient client;
    private static final String BASE = "http://localhost:8080/subtasks";

    @BeforeEach
    void setUp() throws IOException {
        mgr = new InMemoryTaskManager();
        Epic e = new Epic("E", "D");
        mgr.addEpic(e);
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
    void postSubtasksCreates() throws Exception {
        int epicId = mgr.getEpics().getFirst().getId();
        Subtask subtask = new Subtask("Subtask", "Description",
                LocalDateTime.now(), Duration.ofMinutes(5), epicId);
        String body = gson.toJson(subtask);
        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder(URI.create(BASE))
                        .POST(HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(201, resp.statusCode());
        assertEquals(1, mgr.getSubtasks().size());
    }

    @Test
    void getSubtasksLists() throws Exception {
        int epicId = mgr.getEpics().getFirst().getId();
        Subtask subtask = new Subtask("Subtask", "Description",
                LocalDateTime.now(), Duration.ofMinutes(5), epicId);
        String body = gson.toJson(subtask);
        client.send(
                HttpRequest.newBuilder(URI.create(BASE))
                        .POST(HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString()
        );

        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder(URI.create(BASE)).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, resp.statusCode());
        Subtask[] arr = gson.fromJson(resp.body(), Subtask[].class);
        assertEquals(1, arr.length);
        assertEquals("Subtask", arr[0].getName());
    }

    @Test
    void getSubtaskById200() throws Exception {
        int epicId = mgr.getEpics().getFirst().getId();
        Subtask subtask = new Subtask("Subtask", "Description",
                LocalDateTime.now(), Duration.ofMinutes(5), epicId);
        String body = gson.toJson(subtask);
        HttpResponse<String> postResp = client.send(
                HttpRequest.newBuilder(URI.create(BASE))
                        .POST(HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(201, postResp.statusCode());

        int subtaskId = mgr.getSubtasks().getFirst().getId();

        HttpResponse<String> getResp = client.send(
                HttpRequest.newBuilder(URI.create(BASE + "/" + subtaskId)).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, getResp.statusCode());

        Subtask fetched = gson.fromJson(getResp.body(), Subtask.class);
        assertEquals("Subtask", fetched.getName());
        assertEquals(subtaskId, fetched.getId());
    }

    @Test
    void getSubtaskById404() throws Exception {
        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder(URI.create(BASE + "/999")).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(404, resp.statusCode());
    }

    @Test
    void deleteSubtaskById() throws Exception {
        int epicId = mgr.getEpics().getFirst().getId();
        Subtask subtask = new Subtask("Subtask", "Description",
                LocalDateTime.now(), Duration.ofMinutes(5), epicId);
        String body = gson.toJson(subtask);
        HttpResponse<String> postResp = client.send(
                HttpRequest.newBuilder(URI.create(BASE))
                        .POST(HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(201, postResp.statusCode());

        int subtaskId = mgr.getSubtasks().getFirst().getId();

        HttpResponse<String> deleteResp = client.send(
                HttpRequest.newBuilder(URI.create(BASE + "/" + subtaskId))
                        .DELETE().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, deleteResp.statusCode());
        assertNull(mgr.getSubtaskById(subtaskId));
    }
}