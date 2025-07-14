package manager;

import task.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.StringJoiner;

public class Converter {
    public static String toString(Task task) {
        StringJoiner stringJoiner = new StringJoiner(",");
        stringJoiner.add(String.valueOf(task.getId()))
                .add(task.getType().name())
                .add(task.getName())
                .add(task.getStatus().name())
                .add(task.getDescription())
                .add(task.getType() == TaskType.SUBTASK ? String.valueOf(((Subtask) task).getEpicId()) : "")
                .add(task.getStartTime() != null ? task.getStartTime().toString() : "")
                .add(task.getDuration() != null ? String.valueOf(task.getDuration().toMinutes()) : "");
        return stringJoiner.toString();
    }

    public static Task fromString(String value) {
        String[] parts = value.split(",", 8);
        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String name = parts[2];
        Status status = Status.valueOf(parts[3]);
        String description = parts[4];
        String startStr = parts.length > 6 ? parts[6] : "";
        String durationStr = parts.length > 7 ? parts[7] : "";

        LocalDateTime start = startStr.isEmpty() ? null : LocalDateTime.parse(startStr);
        Duration duration = durationStr.isEmpty() ? null : Duration.ofMinutes(Long.parseLong(durationStr));
        Task task = null;
        switch (type) {
            case TASK:
                task = new Task(name, description, start, duration);
                break;
            case EPIC:
                task = new Epic(name, description);
                break;
            case SUBTASK:
                int epicId = Integer.parseInt(parts[5]);
                task = new Subtask(name, description, start, duration, epicId);
                break;
        }
        task.setId(id);
        task.setStatus(status);
        return task;
    }
}