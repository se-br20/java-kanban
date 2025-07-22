package http.handlers;

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
            String[] parts = exchange.getRequestURI().getPath().split("/");

            switch (method) {
                case "GET":
                    if (parts.length == 2) {
                        sendText(exchange, gson.toJson(manager.getSubtasks()));
                    } else if (parts.length == 3) {
                        int id = Integer.parseInt(parts[2]);
                        Subtask sub = manager.getSubtaskById(id);
                        if (sub == null) throw new NoSuchElementException();
                        sendText(exchange, gson.toJson(sub));
                    } else {
                        sendNotFound(exchange);
                    }
                    break;
                case "POST":
                    String body = readBody(exchange);
                    Subtask sub = gson.fromJson(body, Subtask.class);
                    try {
                        if (sub.getId() == 0) manager.addSubtask(sub);
                        else manager.updateSubtask(sub);
                        sendCreated(exchange);
                    } catch (IllegalArgumentException e) {
                        sendOverlap(exchange);
                    }
                    break;
                case "DELETE":
                    if (parts.length == 3) {
                        int id = Integer.parseInt(parts[2]);
                        manager.removeSubtaskById(id);
                        sendOk(exchange);
                    } else {
                        sendNotFound(exchange);
                    }
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
}