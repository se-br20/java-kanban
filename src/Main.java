import task.*;

public class Main {
    public static void main(String[] args) {
        // Создаем менеджер задач
        TaskManager taskManager = new TaskManager();

        // Создаем две обычные задачи
        Task task1 = new Task("Task 1", "Описание задачи");
        Task task2 = new Task("Task 2", "Описание задачи");

        taskManager.addTask(task1);
        taskManager.addTask(task2);

        // Создаем первый эпик с двумя подзадачами
        Epic epic1 = new Epic("Epic 1", "Описание эпика");
        taskManager.addEpic(epic1);

        Subtask subtask1 = new Subtask("Subtask 1", "Описание подзадачи", epic1.getId());
        Subtask subtask2 = new Subtask("Subtask 2", "Описание подзадачи", epic1.getId());

        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        // Создаем второй эпик с одной подзадачей
        Epic epic2 = new Epic("Epic 2", "Описание эпика");
        taskManager.addEpic(epic2);

        Subtask subtask3 = new Subtask("Subtask 3", "Описание подзадачи", epic2.getId());
        taskManager.addSubtask(subtask3);

        // Выводим списки задач, эпиков и подзадач
        System.out.println("Список задач:");
        System.out.println(taskManager.getTasks());

        System.out.println("\nСписок эпиков:");
        System.out.println(taskManager.getEpics());

        System.out.println("\nСписок подзадач:");
        System.out.println(taskManager.getSubtasks());
    }
}
