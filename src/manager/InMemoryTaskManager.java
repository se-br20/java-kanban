package manager;

import task.Epic;
import task.Status;
import task.Subtask;
import task.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {
    private int counterId = 1;
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HistoryManager historyManager;

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    @Override
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void removeTasks() {
        for (int taskId : new ArrayList<>(tasks.keySet())) {
            historyManager.remove(taskId);
        }
        tasks.clear();
    }

    @Override
    public void removeEpics() {
        for (int epicId : new ArrayList<>(epics.keySet())) {
            historyManager.remove(epicId);
        }
        for (int subtaskId : new ArrayList<>(subtasks.keySet())) {
            historyManager.remove(subtaskId);
        }
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void removeSubtasks() {
        for (int subtaskId : new ArrayList<>(subtasks.keySet())) {
            historyManager.remove(subtaskId);
        }
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
            epic.setStatus(Status.NEW);
        }
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    @Override
    public Task addTask(Task task) {
        task.setId(counterId++);
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public Epic addEpic(Epic epic) {
        epic.setId(counterId++);
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public Subtask addSubtask(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            System.out.println("Эпик с id: " + subtask.getEpicId() + " не найден.");
            return null;
        }
        subtask.setId(counterId++);
        subtasks.put(subtask.getId(), subtask);
        epic.addSubtask(subtask.getId());
        updateEpicStatus(epic.getId());
        return subtask;
    }

    @Override
    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            Epic newEpic = epics.get(epic.getId());
            newEpic.setName(epic.getName());
            newEpic.setDescription(epic.getDescription());
            updateEpicStatus(newEpic.getId());
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId())) {
            subtasks.put(subtask.getId(), subtask);
            updateEpicStatus(subtask.getEpicId());
        }
    }

    @Override
    public void removeTaskById(int id) {
        tasks.remove(id);
        historyManager.remove(id);

    }

    @Override
    public void removeEpicById(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (int subtaskId : new ArrayList<>(epic.getSubtaskIds())) {
                subtasks.remove(subtaskId);
                historyManager.remove(subtaskId);
            }
            historyManager.remove(id);
        }
    }

    @Override
    public void removeSubtaskById(int id) {
        if (subtasks.containsKey(id)) {
            Subtask subtask = subtasks.get(id);
            subtasks.remove(id);
            historyManager.remove(id);
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtask(id);
                updateEpicStatus(epic.getId());
            }
        }
    }

    @Override
    public List<Subtask> getSubtasksEpic(int epicId) {
        ArrayList<Subtask> array = new ArrayList<>();
        Epic epic = epics.get(epicId);
        if (epic != null) {
            for (int subtaskId : epic.getSubtaskIds()) {
                array.add(subtasks.get(subtaskId));
            }
        }
        return array;
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return;
        if (epic.getSubtaskIds().isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (int subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask == null) {
                continue;
            }

            switch (subtask.getStatus()) {
                case IN_PROGRESS:
                    epic.setStatus(Status.IN_PROGRESS);
                    return;
                case NEW:
                    allDone = false;
                    break;
                case DONE:
                    allNew = false;
                    break;
                default:
                    break;
            }
        }

        if (allNew) {
            epic.setStatus(Status.NEW);
        } else if (allDone) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }


}