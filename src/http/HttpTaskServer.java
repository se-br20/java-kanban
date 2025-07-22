package http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import http.adapters.DurationAdapter;
import http.adapters.LocalDateTimeAdapter;
import http.handlers.*;
import manager.TaskManager;
import manager.Managers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {
    private final HttpServer server;
    private static final int PORT = 8080;
    private static Gson gsonStatic;

    public HttpTaskServer(TaskManager manager) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);
        gsonStatic = createGson();

        server.createContext("/tasks", new TaskHandler(manager, gsonStatic));
        server.createContext("/subtasks", new SubtaskHandler(manager, gsonStatic));
        server.createContext("/epics", new EpicHandler(manager, gsonStatic));
        server.createContext("/history", new HistoryHandler(manager, gsonStatic));
        server.createContext("/prioritized", new PrioritizedHandler(manager, gsonStatic));
    }

    private static Gson createGson(){
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
    }

    public static Gson getGson() {
        return gsonStatic;
    }

    public void start() {
        server.start();
        System.out.println("HTTP Task Server started on port " + PORT);
    }

    public void stop() {
        server.stop(1);
        System.out.println("HTTP Task Server stopped");
    }

    public static void main(String[] args) throws IOException {
        TaskManager manager = Managers.getDefault();
        new HttpTaskServer(manager).start();
    }
}