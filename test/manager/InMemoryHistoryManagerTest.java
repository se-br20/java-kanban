package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.*;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    private InMemoryHistoryManager historyManager;
    private Task task;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
        task = new Task("Task 1", "Description");
        task.setId(1);
    }

    @Test
    void shouldAddTaskToHistory() {
        historyManager.add(task);
        List<Task> history = historyManager.getHistory();

        assertNotNull(history, "История не должна быть пустой");
        assertEquals(1, history.size(), "История должна содержать одну задачу");
        assertEquals(task, history.get(0), "Задача в истории должна совпадать с добавленной");
    }

    @Test
    void shouldPreservePreviousTaskDataInHistory() {
        task.setDescription("Updated description");
        historyManager.add(task);
        List<Task> history = historyManager.getHistory();

        assertEquals("Updated description", history.get(0).getDescription(),
                "История должна сохранять обновлённые данные задачи");
    }
}
