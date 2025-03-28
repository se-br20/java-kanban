import manager.Managers;
import manager.TaskManager;
import task.*;

import java.util.List;

public class Main {
    public static void main(String[] args) {

        TaskManager taskManager = Managers.getDefault();

        Task task1 = new Task("Task 1", "Описание задачи");
        Task task2 = new Task("Task 2", "Описание задачи");

        taskManager.addTask(task1);
        taskManager.addTask(task2);

        Epic epic1 = new Epic("Epic 1", "Описание эпика");
        taskManager.addEpic(epic1);

        Subtask subtask1 = new Subtask("Subtask 1", "Описание подзадачи", epic1.getId());
        Subtask subtask2 = new Subtask("Subtask 2", "Описание подзадачи", epic1.getId());

        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        Epic epic2 = new Epic("Epic 2", "Описание эпика");
        taskManager.addEpic(epic2);

        Subtask subtask3 = new Subtask("Subtask 3", "Описание подзадачи", epic2.getId());
        taskManager.addSubtask(subtask3);

        System.out.println("\nСписок задач:");
        System.out.println(taskManager.getTasks());

        System.out.println("\nПолучение Task 1 по ID:");
        System.out.println(taskManager.getTaskById(task1.getId()));

        System.out.println("\nПолучение Task 2 по ID:");
        System.out.println(taskManager.getTaskById(task2.getId()));

        System.out.println("\nСписок эпиков:");
        System.out.println(taskManager.getEpics());

        System.out.println("\nСписок подзадач:");
        System.out.println(taskManager.getSubtasks());

        System.out.println("\nПодзадачи для Epic 1:");
        List<Subtask> epic1Subtasks = taskManager.getSubtasksEpic(epic1.getId());
        System.out.println(epic1Subtasks);

        System.out.println("\nОбновление Task 2:");
        task2.setDescription("Обновленное описание задачи");
        taskManager.updateTask(task2);
        System.out.println(taskManager.getTasks());

        System.out.println("\nОбновление Epic 1:");
        epic1.setDescription("Обновленное описание эпика");
        taskManager.updateEpic(epic1);
        System.out.println(taskManager.getEpics());

        System.out.println("\nПолучение Epic 1 по ID:");
        System.out.println(taskManager.getEpicById(epic1.getId()));


        System.out.println("\nПолучение Subtask 2 по ID:");
        System.out.println(taskManager.getSubtaskById(subtask2.getId()));

        System.out.println("\nОбновление статуса Subtask 1:");
        subtask1.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask1);
        System.out.println(taskManager.getSubtasks());

        System.out.println("\nОбновленный Epic 1 (статус изменился?):");
        System.out.println(taskManager.getEpicById(epic1.getId()));

        System.out.println("\nИстория просмотров:");
        System.out.println(taskManager.getHistory());

        taskManager.removeTaskById(task1.getId());
        System.out.println("\nПосле удаления Task 1:");
        System.out.println(taskManager.getTasks());

        taskManager.removeSubtaskById(subtask1.getId());
        System.out.println("\nПосле удаления Subtask 1:");
        System.out.println(taskManager.getSubtasks());
        System.out.println("Эпики после удаления Subtask 1:");
        System.out.println(taskManager.getEpics());

        taskManager.removeEpicById(epic2.getId());
        System.out.println("\nПосле удаления Epic 2:");
        System.out.println(taskManager.getEpics());
        System.out.println("Оставшиеся подзадачи:");
        System.out.println(taskManager.getSubtasks());

        taskManager.removeTasks();
        System.out.println("\nПосле удаления всех задач:");
        System.out.println(taskManager.getTasks());

        taskManager.removeSubtasks();
        System.out.println("\nПосле удаления всех подзадач:");
        System.out.println(taskManager.getSubtasks());

        System.out.println("\nСписок эпиков после удаления подзадач:");
        System.out.println(taskManager.getEpics());

        taskManager.removeEpics();
        System.out.println("\nПосле удаления всех эпиков:");
        System.out.println(taskManager.getEpics());

        System.out.println("\nИстория просмотров после удаления всех задач, подзадач и эпиков:");
        System.out.println(taskManager.getHistory());
    }
}