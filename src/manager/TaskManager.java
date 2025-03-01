package manager;

import task.Epic;
import task.Subtask;
import task.Task;
import java.util.ArrayList;

public interface TaskManager {
    ArrayList<Task> getTasks();
    ArrayList<Epic> getEpics();
    ArrayList<Subtask> getSubtasks();

    Task getTaskById(int id);
    Epic getEpicById(int id);
    Subtask getSubtaskById(int id);

    Task addTask(Task task);
    Epic addEpic(Epic epic);
    Subtask addSubtask(Subtask subtask);

    void updateTask(Task task);
    void updateEpic(Epic epic);
    void updateSubtask(Subtask subtask);

    void removeTaskById(int id);
    void removeEpicById(int id);
    void removeSubtaskById(int id);

    void removeTasks();
    void removeEpics();
    void removeSubtasks();

    ArrayList<Subtask> getSubtasksEpic(int epicId);
    ArrayList<Task> getHistory();
}