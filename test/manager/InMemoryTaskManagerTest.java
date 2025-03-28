package manager;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.*;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

    private InMemoryTaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager(Managers.getDefaultHistory());
    }

    @Test
    void shouldAddAndFindTaskById() {
        Task task = new Task("Test Task", "Task description");
        taskManager.addTask(task);

        Task retrievedTask = taskManager.getTaskById(task.getId());

        assertNotNull(retrievedTask, "Задача должна существовать");
        assertEquals(task, retrievedTask, "Добавленная и полученная задачи должны совпадать");
    }

    @Test
    void shouldRemoveEpicAndSubtasks() {
        Epic epic = new Epic("Epic 1", "Epic description");
        taskManager.addEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description", epic.getId());
        Subtask subtask2 = new Subtask("Subtask 2", "Description", epic.getId());

        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        taskManager.removeEpicById(epic.getId());

        assertNull(taskManager.getEpicById(epic.getId()), "Эпик должен быть удалён");
        assertNull(taskManager.getSubtaskById(subtask1.getId()), "Подзадача 1 должна быть удалена");
        assertNull(taskManager.getSubtaskById(subtask2.getId()), "Подзадача 2 должна быть удалена");
    }

    @Test
    void shouldNotHaveOrphanedSubtasksAfterEpicRemoval() {
        Epic epic = new Epic("Epic 1", "Epic description");
        taskManager.addEpic(epic);

        Subtask subtask = new Subtask("Subtask", "Description", epic.getId());
        taskManager.addSubtask(subtask);

        taskManager.removeEpicById(epic.getId());

        assertFalse(taskManager.getSubtasks().contains(subtask), "После удаления эпика подзадачи не должны оставаться");
    }

    @Test
    void shouldUpdateTaskFieldsAndKeepDataIntegrity() {
        Task task = new Task("Task", "Description");
        taskManager.addTask(task);

        task.setDescription("Updated description");
        taskManager.updateTask(task);

        Task updatedTask = taskManager.getTaskById(task.getId());
        assertEquals("Updated description", updatedTask.getDescription(), "Описание задачи должно обновиться");
    }

    @Test
    void shouldNotAddSubtaskToNonexistentEpic() {
        Subtask subtask = new Subtask("Subtask", "Description", 999); // Несуществующий ID эпика

        taskManager.addSubtask(subtask);

        assertNull(taskManager.getSubtaskById(subtask.getId()), "Подзадача не должна быть добавлена без существующего эпика");
    }
}
