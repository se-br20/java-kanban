package task;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(String name, String description, int epicId) {
        super(name, description);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return "Subtask{id = " + getId() +
                ", name= '" + getName() + "'" +
                ", description= '" + getDescription() + "'" +
                ", status= " + getStatus() +
                ", epicId= " + getEpicId() +
                '}';
    }
}
