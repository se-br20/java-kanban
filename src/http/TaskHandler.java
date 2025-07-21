package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import task.Task;

import java.io.IOException;
import java.net.URI;
import java.util.NoSuchElementException;

public class TaskHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public TaskHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if ("GET".equals(method)) {
                if (path.matches("/tasks/?")) {
                    String json = gson.toJson(manager.getTasks());
                    sendText(exchange, json, 200);
                } else if (path.matches("/tasks/\\d+")) {
                    int id = Integer.parseInt(path.substring(path.lastIndexOf('/') + 1));
                    Task task = manager.getTaskById(id);
                    if (task == null) throw new NoSuchElementException();
                    sendText(exchange, gson.toJson(task), 200);
                } else {
                    sendNotFound(exchange);
                }
            } else if ("POST".equals(method) && path.matches("/tasks/?")) {
                String body = readBody(exchange);
                Task task = gson.fromJson(body, Task.class);
                try {
                    if (task.getId() == 0) {
                        manager.addTask(task);
                    } else {
                        manager.updateTask(task);
                    }
                    sendEmpty(exchange, 201);
                } catch (IllegalArgumentException e) {
                    sendOverlap(exchange);
                }
            } else if ("DELETE".equals(method) && path.matches("/tasks/\\d+")) {
                int id = Integer.parseInt(path.substring(path.lastIndexOf('/') + 1));
                manager.removeTaskById(id);
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