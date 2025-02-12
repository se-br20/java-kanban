package task;

import java.util.ArrayList;

public class Epic extends Task {
    Status status = Status.NEW;
    private ArrayList<Integer> subtaskIds;

    public Epic(String name, String description) {
        super(name, description);
        subtaskIds = new ArrayList<>();
    }

    public ArrayList<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void addSubtask(int subtaskId) {
        subtaskIds.add(subtaskId);
    }

    public void removeSubtask(int subtaskId) {

        subtaskIds.remove(Integer.valueOf(subtaskId));
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id = " + id +
                ", name = " + name +
                ", description = " + description +
                ", status = " + status +
                ", subtasks = " + subtaskIds +
                '}';
    }

}
