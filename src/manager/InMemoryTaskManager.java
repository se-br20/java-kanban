package manager;

import task.Epic;
import task.Status;
import task.Subtask;
import task.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    protected int counterId = 1;
    protected final HashMap<Integer, Task> tasks = new HashMap<>();
    protected final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    protected final HashMap<Integer, Epic> epics = new HashMap<>();
    protected final HistoryManager historyManager;

    public InMemoryTaskManager() {
        this.historyManager = Managers.getDefaultHistory();
    }

    private final NavigableSet<Task> prioritized = new TreeSet<>(
            Comparator.comparing(Task::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparingInt(Task::getId));

    private void validateTime(Task task) {
        if (task.getStartTime() == null || task.getDuration() == null) {
            throw new IllegalArgumentException("startTime и duration обязательны");
        }
    }

    public static boolean isOverlap(Task firstTask, Task secondTask) {
        if (firstTask.getStartTime() == null || secondTask.getStartTime() == null) return false;
        return !firstTask.getEndTime().isBefore(secondTask.getStartTime())
                && !secondTask.getEndTime().isBefore(firstTask.getStartTime());
    }

    private void checkOverlap(Task newTask) {
        if (prioritized.stream().anyMatch(existingTask -> isOverlap(existingTask, newTask))) {
            throw new IllegalArgumentException("Пересечение при задаче " + newTask.getId());
        }
    }

    private void addPrioritized(Task task) {
        if (task.getStartTime() != null && task.getDuration() != null) {
            checkOverlap(task);
            prioritized.add(task);
        }
    }

    protected void rebuildPrioritized() {
        prioritized.clear();
        tasks.values().stream()
                .filter(t -> t.getStartTime() != null && t.getDuration() != null)
                .forEach(this::addPrioritized);
        subtasks.values().stream()
                .filter(t -> t.getStartTime() != null && t.getDuration() != null)
                .forEach(this::addPrioritized);
    }

    protected void updateEpicTime(Epic epic) {
        List<Subtask> tasks = epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .toList();
        if (tasks.isEmpty()) {
            epic.setStartTime(null);
            epic.setDuration(Duration.ZERO);
            epic.setEndTime(null);
            return;
        }
        LocalDateTime minStartTime = tasks.stream()
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);
        LocalDateTime maxEndTime = tasks.stream()
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        Duration durationTime = tasks.stream()
                .map(Subtask::getDuration)
                .filter(Objects::nonNull)
                .reduce(Duration.ZERO, Duration::plus);

        epic.setStartTime(minStartTime);
        epic.setDuration(durationTime);
        epic.setEndTime(maxEndTime);
    }

    @Override
    public Task addTask(Task task) {
        validateTime(task);
        task.setId(counterId++);
        addPrioritized(task);
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
        validateTime(subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            System.out.println("Эпик с id: " + subtask.getEpicId() + " не найден.");
            return null;
        }
        subtask.setId(counterId++);
        addPrioritized(subtask);
        subtasks.put(subtask.getId(), subtask);
        epic.addSubtask(subtask.getId());
        updateEpicTime(epic);
        updateEpicStatus(epic.getId());
        return subtask;
    }

    @Override
    public void removeTasks() {
        tasks.keySet().forEach(historyManager::remove);
        tasks.clear();
        rebuildPrioritized();
    }

    @Override
    public void removeEpics() {
        epics.keySet().forEach(historyManager::remove);
        subtasks.keySet().forEach(historyManager::remove);
        epics.clear();
        subtasks.clear();
        rebuildPrioritized();
    }

    @Override
    public void removeSubtasks() {
        subtasks.keySet().forEach(historyManager::remove);
        subtasks.clear();
        epics.values().forEach(epic -> epic.getSubtaskIds().clear());
        rebuildPrioritized();
    }

    @Override
    public void removeTaskById(int id) {
        tasks.remove(id);
        historyManager.remove(id);
        rebuildPrioritized();
    }

    @Override
    public void removeEpicById(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            epic.getSubtaskIds().forEach(subtasks::remove);
            historyManager.remove(id);
            rebuildPrioritized();
        }
    }

    @Override
    public void removeSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            historyManager.remove(id);
            rebuildPrioritized();
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtask(id);
                updateEpicStatus(epic.getId());
                updateEpicTime(epic);
            }
        }
    }

    @Override
    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            validateTime(task);
            tasks.put(task.getId(), task);
            rebuildPrioritized();
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
            validateTime(subtask);
            subtasks.put(subtask.getId(), subtask);
            rebuildPrioritized();
            updateEpicTime(epics.get(subtask.getEpicId()));
            updateEpicStatus(subtask.getEpicId());
        }
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
    public List<Subtask> getSubtasksEpic(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return Collections.emptyList();
        return epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    protected void updateEpicStatus(int epicId) {
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

    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritized);
    }
}