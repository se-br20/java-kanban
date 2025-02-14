import manager.TaskManager;
import task.*;
// Владислав, привет, спасибо за корректировки, хороших выходных.
public class Main {
    public static void main(String[] args) {

        TaskManager taskManager = new TaskManager();

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

        System.out.println("Список задач:");
        System.out.println(taskManager.getTasks());

        System.out.println("\nСписок эпиков:\n");
        System.out.println(taskManager.getEpics());

        System.out.println("\nСписок подзадач:\n");
        System.out.println(taskManager.getSubtasks());

        subtask1.setStatus(Status.DONE);
        subtask2.setStatus(Status.IN_PROGRESS);
        taskManager.updateSubtask(subtask1);
        taskManager.updateSubtask(subtask2);
        System.out.println("\nСмена статуса\n");
        System.out.println(taskManager.getEpicById(epic1.getId()));
        System.out.println(taskManager.getEpicById(epic1.getId()));

        taskManager.removeTaskById(task1.getId());
        System.out.println("\nПосле удаления Task 1:\n");
        System.out.println(taskManager.getTasks());

        taskManager.removeSubtaskById(subtask1.getId());
        System.out.println("\nПосле удаления Subtask 1:\n");
        System.out.println(taskManager.getEpics());
        System.out.println(taskManager.getSubtasks());

        taskManager.removeEpicById(epic2.getId());
        System.out.println("\nПосле удаления Epic 2:\n");
        System.out.println(taskManager.getEpics());
        System.out.println(taskManager.getSubtasks());
    }

}
