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
    void shouldNotConflictWithGeneratedAndManualId() {
        Task task1 = new Task("Task 1", "Description");
        task1.setId(5);
        taskManager.addTask(task1);

        Task task2 = new Task("Task 2", "Description");
        taskManager.addTask(task2);

        assertNotEquals(5, task2.getId(), "Генерируемый ID не должен конфликтовать с установленным вручную");
    }

    @Test
    void shouldPreserveTaskDataWhenAdded() {
        Task task = new Task("Original Task", "Original description");
        taskManager.addTask(task);

        Task retrievedTask = taskManager.getTaskById(task.getId());

        assertEquals("Original Task", retrievedTask.getName());
        assertEquals("Original description", retrievedTask.getDescription());
    }
}
