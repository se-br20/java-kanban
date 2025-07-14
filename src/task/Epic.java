package task;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private LocalDateTime endTime;
    private final List<Integer> subtaskIds = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description, null, null);
    }

    public void addSubtask(int subtaskId) {
        subtaskIds.add(subtaskId);
    }

    public void removeSubtask(int subtaskId) {
        subtaskIds.remove(Integer.valueOf(subtaskId));
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }


    @Override
    public String toString() {
        return "Epic{id = " + getId() +
                ", name = '" + getName() + "'" +
                ", description = '" + getDescription() + "'" +
                ", status = " + getStatus() +
                ", startTime = " + startTime +
                ", duration = " + duration +
                ", endTime = " + endTime +
                ", subtasks = " + subtaskIds +
                '}';
    }

}
