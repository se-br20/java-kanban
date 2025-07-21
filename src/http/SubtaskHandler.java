package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import task.Subtask;

import java.io.IOException;
import java.util.NoSuchElementException;

public class SubtaskHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public SubtaskHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if ("GET".equals(method)) {
                if (path.matches("/subtasks/?")) {
                    sendText(exchange, gson.toJson(manager.getSubtasks()), 200);
                } else if (path.matches("/subtasks/\\d+")) {
                    int id = Integer.parseInt(path.substring(path.lastIndexOf('/') + 1));
                    Subtask st = manager.getSubtaskById(id);
                    if (st == null) throw new NoSuchElementException();
                    sendText(exchange, gson.toJson(st), 200);
                } else {
                    sendNotFound(exchange);
                }
            } else if ("POST".equals(method) && path.matches("/subtasks/?")) {
                String body = readBody(exchange);
                Subtask sub = gson.fromJson(body, Subtask.class);
                try {
                    if (sub.getId() == 0) {
                        manager.addSubtask(sub);
                    } else {
                        manager.updateSubtask(sub);
                    }
                    sendEmpty(exchange, 201);
                } catch (IllegalArgumentException e) {
                    sendOverlap(exchange);
                }
            } else if ("DELETE".equals(method) && path.matches("/subtasks/\\d+")) {
                int id = Integer.parseInt(path.substring(path.lastIndexOf('/') + 1));
                manager.removeSubtaskById(id);
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