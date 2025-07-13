package manager;

import task.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;



public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File file;

    public FileBackedTaskManager(Path filePath) {
        super();
        this.file = filePath.toFile();
    }

    private void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("id,type,name,status,description,epic,startTime,duration\n");
            for (Task task : tasks.values()) {
                writer.write(Converter.toString(task) + "\n");
            }
            for (Epic epic : epics.values()) {
                writer.write(Converter.toString(epic) + "\n");
            }
            for (Subtask subtask : subtasks.values()) {
                writer.write(Converter.toString(subtask) + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении файла: " + file.getName(), e);
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file.toPath());
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) {
                    continue;
                }
                Task task = Converter.fromString(line);
                int id = task.getId();

                switch (task.getType()) {
                    case TASK:
                        manager.tasks.put(id, task);
                        break;
                    case EPIC:
                        manager.epics.put(id, (Epic) task);
                        break;
                    case SUBTASK:
                        Subtask subtask = (Subtask) task;
                        manager.subtasks.put(id, subtask);
                        manager.epics.get(subtask.getEpicId()).addSubtask(id);
                        break;
                }
                if (manager.counterId <= id) {
                    manager.counterId = id + 1;
                }
            }
            manager.epics.values().forEach(e -> {
                manager.updateEpicTime(e);
                manager.updateEpicStatus(e.getId());
            });
            manager.rebuildPrioritized();
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при загрузке из файла: " + file.getName(), e);
        }
        return manager;
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
        return super.getTaskById(id);
    }

    @Override
    public Epic getEpicById(int id) {
        return super.getEpicById(id);
    }

    @Override
    public Subtask getSubtaskById(int id) {
        return super.getSubtaskById(id);
    }

    public static void main(String[] args) {
        File file = new File("tasks.csv");

        FileBackedTaskManager manager = new FileBackedTaskManager(file.toPath());

        Task task = new Task("Task", "Описание Task", LocalDateTime.now(), Duration.ofMinutes(30));
        manager.addTask(task);

        Epic epic = new Epic("Epic", "Описание Epic");
        manager.addEpic(epic);

        Subtask subtask1 = new Subtask("Subtask1", "Описание Subtask1",
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(45), epic.getId());
        manager.addSubtask(subtask1);
        Subtask subtask2 = new Subtask("Subtask2", "Описание Subtask2",
                LocalDateTime.now().plusHours(2), Duration.ofMillis(60), epic.getId());
        manager.addSubtask(subtask2);

        manager.getTaskById(task.getId());
        manager.getEpicById(epic.getId());
        manager.getSubtaskById(subtask1.getId());
        manager.getSubtaskById(subtask2.getId());

        FileBackedTaskManager loadManager = FileBackedTaskManager.loadFromFile(file);

        System.out.println("Задачи: " + loadManager.getTasks());
        System.out.println("Эпики: " + loadManager.getEpics());
        System.out.println("Подзадачи: " + loadManager.getSubtasks());

    }
}

