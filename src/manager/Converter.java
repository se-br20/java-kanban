package manager;

import task.*;

import java.util.StringJoiner;

public class Converter {
    public static String toString(Task task) {
        StringJoiner stringJoiner = new StringJoiner(",");
        stringJoiner.add(String.valueOf(task.getId())).add(task.getType().name()).add(task.getName())
                .add(task.getStatus().name()).add(task.getDescription()).add(task.getType() == TaskType.SUBTASK
                        ? String.valueOf(((Subtask) task).getEpicId()) : "");
        return stringJoiner.toString();
    }

    public static Task fromString(String value) {
        String[] parts = value.split(",",6);
        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String name = parts[2];
        Status status = Status.valueOf(parts[3]);
        String description = parts[4];

        Task task = null;
        switch (type) {
            case TASK:
                task = new Task(name, description);
                break;
            case EPIC:
                task = new Epic(name, description);
                break;
            case SUBTASK:
                int epicId = Integer.parseInt(parts[5]);
                task = new Subtask(name, description, epicId);
                break;
        }
        task.setId(id);
        task.setStatus(status);
        return task;
    }
}