package http;

import com.google.gson.Gson;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.*;
import task.Epic;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskManagerEpicsTest {
    private HttpTaskServer server;
    private TaskManager mgr;
    private Gson gson;
    private HttpClient client;
    private static final String BASE = "http://localhost:8080/epics";

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
    void postEpicsCreates() throws Exception {
        Epic epic = new Epic("Epic", "Description");
        String body = gson.toJson(epic);
        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder(URI.create(BASE))
                        .POST(HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(201, resp.statusCode());
        assertEquals(1, mgr.getEpics().size());
    }

    @Test
    void getEpicsLists() throws Exception {
        Epic epic = new Epic("Epic", "Description");
        String body = gson.toJson(epic);
        client.send(HttpRequest.newBuilder(URI.create(BASE))
                        .POST(HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString());

        Epic created = mgr.getEpics().getFirst();

        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder(URI.create(BASE)).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, resp.statusCode());
        Epic[] arr = gson.fromJson(resp.body(), Epic[].class);
        assertEquals(1, arr.length);
        assertEquals(created.getId(), arr[0].getId());
        assertEquals(created.getName(), arr[0].getName());
    }

    @Test
    void getEpicById200() throws Exception {
        Epic epic = new Epic("Epic", "Description");
        String body = gson.toJson(epic);
        client.send(HttpRequest.newBuilder(URI.create(BASE))
                        .POST(HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString());

        int id = mgr.getEpics().getFirst().getId();

        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder(URI.create(BASE + "/" + id)).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, resp.statusCode());
    }

    @Test
    void getEpicById404() throws Exception {
        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder(URI.create(BASE + "/735")).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(404, resp.statusCode());
    }

    @Test
    void deleteEpicById() throws Exception {
        Epic epic = new Epic("Epic", "Description");
        String body = gson.toJson(epic);
        client.send(HttpRequest.newBuilder(URI.create(BASE))
                        .POST(HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString());

        int id = mgr.getEpics().getFirst().getId();

        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder(URI.create(BASE + "/" + id))
                        .DELETE().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, resp.statusCode());
        assertNull(mgr.getEpicById(id));
    }
}
