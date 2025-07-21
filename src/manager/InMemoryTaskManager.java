package manager;

import task.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    protected int counterId = 1;
    protected final HashMap<Integer, Task> tasks = new HashMap<>();
    protected final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    protected final HashMap<Integer, Epic> epics = new HashMap<>();
    protected final HistoryManager historyManager;
    protected final NavigableSet<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(Task::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparingInt(Task::getId));

    public InMemoryTaskManager() {
        this.historyManager = Managers.getDefaultHistory();
    }

    private static boolean isOverlap(Task firstTask, Task secondTask) {
        return firstTask.getStartTime().isBefore(secondTask.getEndTime()) && secondTask.getStartTime()
                        .isBefore(firstTask.getEndTime());
    }

    private void checkOverlap(Task newTask) {
        if (newTask.getStartTime() == null || newTask.getEndTime() == null) return;
        if (prioritizedTasks.stream()
                .filter(existingTask -> existingTask.getId() != newTask.getId())
                .anyMatch(existingTask -> isOverlap(existingTask, newTask))) {
            throw new IllegalArgumentException("Пересечение при задаче " + newTask.getId());
        }
    }

    protected void addPrioritized(Task task) {
        if (task.getStartTime() != null && task.getDuration() != null) {
            checkOverlap(task);
            prioritizedTasks.add(task);
        }
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
                .map(Subtask::getStartTime)
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
        tasks.values().forEach(prioritizedTasks::remove);
        tasks.keySet().forEach(historyManager::remove);
        tasks.clear();
    }

    @Override
    public void removeEpics() {
        subtasks.keySet().forEach(id -> {
            prioritizedTasks.remove(subtasks.get(id));
            historyManager.remove(id);
        });
        epics.keySet().forEach(historyManager::remove);
        subtasks.clear();
        epics.clear();
    }

    @Override
    public void removeSubtasks() {
        subtasks.values().forEach(prioritizedTasks::remove);
        subtasks.keySet().forEach(historyManager::remove);
        subtasks.clear();
        epics.values().forEach(epic -> {
            epic.getSubtaskIds().clear();
            updateEpicTime(epic);
            updateEpicStatus(epic.getId());
        });
    }

    @Override
    public void removeTaskById(int id) {
        Task task = tasks.remove(id);
        if (task != null) {
            prioritizedTasks.remove(task);
            historyManager.remove(id);
        }
    }

    @Override
    public void removeEpicById(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            epic.getSubtaskIds().forEach(sid -> {
                prioritizedTasks.remove(subtasks.remove(sid));
                historyManager.remove(sid);
            });
            historyManager.remove(id);
        }
    }

    @Override
    public void removeSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            prioritizedTasks.remove(subtask);
            historyManager.remove(id);
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
            Task oldTask = tasks.get(task.getId());
            prioritizedTasks.remove(oldTask);
            try {
                addPrioritized(task);
                tasks.put(task.getId(), task);
            } catch (IllegalArgumentException e) {
                prioritizedTasks.add(oldTask);
                throw e;
            }

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
            Subtask oldSubtask = subtasks.get(subtask.getId());
            prioritizedTasks.remove(oldSubtask);
            try {
                addPrioritized(subtask);
                subtasks.put(subtask.getId(), subtask);
            } catch (IllegalArgumentException e) {
                prioritizedTasks.add(oldSubtask);
                throw e;
            }
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
        if (task != null) historyManager.add(task);
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
        return List.copyOf(prioritizedTasks);
    }
}