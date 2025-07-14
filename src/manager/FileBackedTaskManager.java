package manager;

import task.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.io.File;


public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File file;

    public FileBackedTaskManager(Path filePath) {
        super(Managers.getDefaultHistory());
        this.file = filePath.toFile();
    }

    private void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("id,type,name,status,description,epic\n");
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
                        Epic epic = (Epic) task;
                        manager.epics.put(id, epic);
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
        Task dataGetTaskById = super.getTaskById(id);
        return dataGetTaskById;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic dataGetEpicById = super.getEpicById(id);
        return dataGetEpicById;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask dataGetSubtaskById = super.getSubtaskById(id);
        return dataGetSubtaskById;
    }

    public static void main(String[] args) {
        File file = new File("tasks.csv");

        FileBackedTaskManager manager = new FileBackedTaskManager(file.toPath());

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

    }
}

