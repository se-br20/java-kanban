package manager;

import task.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.io.File;


public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File file;

    public FileBackedTaskManager(HistoryManager historyManager, Path filePath) {
        super(historyManager);
        this.file = filePath.toFile();
    }

    private String taskToString(Task task) {
        String[] files = {String.valueOf(task.getId()), task.getType().name(), task.getName(), task.getStatus().name(),
                task.getDescription(), task instanceof Subtask ? String.valueOf(((Subtask) task).getEpicId()) : ""
        };
        return String.join(",", files);
    }

    private Task taskFromString(String value) {
        String[] parts = value.split(",", -1);
        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String name = parts[2];
        Status status = Status.valueOf(parts[3]);
        String description = parts[4];

        Task task;
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
            default:
                throw new IllegalStateException("Неизвестный тип задачи: " + type);
        }
        task.setId(id);
        task.setStatus(status);
        return task;
    }

    private String historyToString(HistoryManager manager) {
        List<Task> history = manager.getHistory();
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < history.size(); i++) {
            builder.append(history.get(i).getId());
            if (i < history.size() - 1) {
                builder.append(",");
            }
        }
        return builder.toString();
    }

    private List<Integer> historyFromString(String value) {
        List<Integer> history = new ArrayList<>();
        if (!value.isEmpty()) {
            String[] ids = value.split(",");
            for (String id : ids) {
                history.add(Integer.parseInt(id));
            }
        }
        return history;
    }

    private void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("id,type,name,status,description,epic\n");

            for (Task task : tasks.values()) {
                writer.write(taskToString(task) + "\n");
            }
            for (Epic epic : epics.values()) {
                writer.write(taskToString(epic) + "\n");
            }
            for (Subtask subtask : subtasks.values()) {
                writer.write(taskToString(subtask) + "\n");
            }
            writer.write("\n");
            writer.write(historyToString(historyManager));
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении файла: " + file.getName(), e);
        }
    }

    private Task getTaskFromAll(int id) {
        if (tasks.containsKey(id)) {
            return tasks.get(id);
        } else if (epics.containsKey(id)) {
            return epics.get(id);
        } else {
            return subtasks.get(id);
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(new InMemoryHistoryManager(), file.toPath());
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            int i = 1;
            while (i < lines.size() && !lines.get(i).isEmpty()) {
                Task task = manager.taskFromString(lines.get(i));
                int id = task.getId();

                if (task instanceof Epic) {
                    manager.epics.put(id, (Epic) task);
                } else if (task instanceof Subtask) {
                    Subtask subtask = (Subtask) task;
                    manager.subtasks.put(id, subtask);
                    Epic epic = manager.epics.get(subtask.getEpicId());
                    epic.addSubtask(subtask.getId());
                } else {
                    manager.tasks.put(id, task);
                }
                if (id > getCounterId()) {
                    manager.setCounterId(id + 1);
                }
                i++;
            }
            if (i < lines.size() && lines.get(i).isBlank()) {
                i++;
            }
            if (i < lines.size()) {
                List<Integer> history = manager.historyFromString(lines.get(i));
                for (int taskId : history) {
                    Task task = manager.getTaskFromAll(taskId);
                    if (task != null) {
                        manager.historyManager.add(task);
                    }
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при загрузке из файла: " + file.getName(), e);
        }
        return manager;
    }

    @Override
    public void removeTasks() {
        super.removeTasks();
        save();
    }

    @Override
    public void removeEpics() {
        super.removeEpics();
        save();
    }

    @Override
    public void removeSubtasks() {
        super.removeSubtasks();
        save();
    }


    @Override
    public Task addTask(Task task) {
        Task dataTask = super.addTask(task);
        save();
        return dataTask;
    }

    @Override
    public Epic addEpic(Epic epic) {
        Epic dataEpic = super.addEpic(epic);
        save();
        return dataEpic;
    }

    @Override
    public Subtask addSubtask(Subtask subtask) {
        Subtask dataSubtask = super.addSubtask(subtask);
        save();
        return dataSubtask;
    }

    @Override
    public void removeTaskById(int id) {
        super.removeTaskById(id);
        save();
    }

    @Override
    public void removeEpicById(int id) {
        super.removeEpicById(id);
        save();
    }

    @Override
    public void removeSubtaskById(int id) {
        super.removeSubtaskById(id);
        save();
    }

    @Override
    public Task getTaskById(int id) {
        Task dataGetTaskById = super.getTaskById(id);
        save();
        return dataGetTaskById;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic dataGetEpicById = super.getEpicById(id);
        save();
        return dataGetEpicById;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask dataGetSubtaskById = super.getSubtaskById(id);
        save();
        return dataGetSubtaskById;
    }

    public static void main(String[] args) {
        File file = new File("tasks.csv");

        FileBackedTaskManager manager = new FileBackedTaskManager(new InMemoryHistoryManager(), file.toPath());
        Task task = new Task("Task", "Описание Task");
        manager.addTask(task);

        Epic epic = new Epic("Epic", "Описание Epic");
        manager.addEpic(epic);

        Subtask subtask1 = new Subtask("Subtask1", "Описание Subtask1", epic.getId());
        manager.addSubtask(subtask1);
        Subtask subtask2 = new Subtask("Subtask2", "Описание Subtask2", epic.getId());
        manager.addSubtask(subtask2);

        manager.getTaskById(task.getId());
        manager.getEpicById(epic.getId());
        manager.getSubtaskById(subtask1.getId());
        manager.getSubtaskById(subtask2.getId());

        FileBackedTaskManager loadManager = FileBackedTaskManager.loadFromFile(file);

        System.out.println("Задачи: " + loadManager.getTasks());
        System.out.println("Эпики: " + loadManager.getEpics());
        System.out.println("Подзадачи: " + loadManager.getSubtasks());
        System.out.println("История: " + loadManager.getHistory());
    }
}

