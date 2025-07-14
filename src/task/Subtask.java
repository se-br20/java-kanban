package task;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(String name, String description, LocalDateTime startTime, Duration duration, int epicId) {
        super(name, description, startTime, duration);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }

    @Override
    public String toString() {
        return "Subtask{id = " + getId() +
                ", name= '" + getName() + "'" +
                ", description= '" + getDescription() + "'" +
                ", status= " + getStatus() +
                ", startTime = " + startTime +
                ", duration = " + duration +
                ", epicId= " + getEpicId() +
                '}';
    }
}
