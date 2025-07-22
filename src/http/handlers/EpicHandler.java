package http.handlers;

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
            String[] parts = exchange.getRequestURI().getPath().split("/");

            switch (method) {
                case "GET":
                    handleGet(exchange, parts);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "DELETE":
                    handleDelete(exchange, parts);
                    break;
                default:
                    sendNotFound(exchange);
            }
        } catch (NoSuchElementException e) {
            sendNotFound(exchange);
        } catch (Exception e) {
            sendServerError(exchange);
        }
    }

    private void handleGet(HttpExchange exchange, String[] parts) throws IOException {
        if (parts.length == 2) {
            sendText(exchange, gson.toJson(manager.getEpics()));
        } else if (parts.length == 3) {
            int id = Integer.parseInt(parts[2]);
            Epic epic = manager.getEpicById(id);
            if (epic == null) throw new NoSuchElementException();
            sendText(exchange, gson.toJson(epic));
        } else if (parts.length == 4 && "subtasks".equals(parts[3])) {
            int id = Integer.parseInt(parts[2]);
            if (manager.getEpicById(id) == null) throw new NoSuchElementException();
            sendText(exchange, gson.toJson(manager.getSubtasksEpic(id)));
        } else {
            sendNotFound(exchange);
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        Epic epic = gson.fromJson(body, Epic.class);
        manager.addEpic(epic);
        sendCreated(exchange);
    }

    private void handleDelete(HttpExchange exchange, String[] parts) throws IOException {
        if (parts.length == 3) {
            int id = Integer.parseInt(parts[2]);
            manager.removeEpicById(id);
            sendOk(exchange);
        } else {
            sendNotFound(exchange);
        }
    }
}