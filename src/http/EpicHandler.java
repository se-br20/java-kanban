package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import task.Epic;

import java.io.IOException;
import java.util.NoSuchElementException;

public class EpicHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public EpicHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if ("GET".equals(method)) {
                if (path.matches("/epics/?")) {
                    sendText(exchange, gson.toJson(manager.getEpics()), 200);
                } else if (path.matches("/epics/\\d+")) {
                    int id = Integer.parseInt(path.substring(path.lastIndexOf('/') + 1));
                    Epic epic = manager.getEpicById(id);
                    if (epic == null) throw new NoSuchElementException();
                    sendText(exchange, gson.toJson(epic), 200);
                } else if (path.matches("/epics/\\d+/subtasks")) {
                    int id = Integer.parseInt(path.split("/")[2]);
                    // проверяем существование
                    if (manager.getEpicById(id) == null) throw new NoSuchElementException();
                    sendText(exchange, gson.toJson(manager.getSubtasksEpic(id)), 200);
                } else {
                    sendNotFound(exchange);
                }
            } else if ("POST".equals(method) && path.matches("/epics/?")) {
                String body = readBody(exchange);
                Epic epic = gson.fromJson(body, Epic.class);
                manager.addEpic(epic);
                sendEmpty(exchange, 201);
            } else if ("DELETE".equals(method) && path.matches("/epics/\\d+")) {
                int id = Integer.parseInt(path.substring(path.lastIndexOf('/') + 1));
                manager.removeEpicById(id);
                sendEmpty(exchange, 200);
            } else {
                sendNotFound(exchange);
            }
        } catch (NoSuchElementException e) {
            sendNotFound(exchange);
        } catch (Exception e) {
            sendServerError(exchange);
        }
    }
}