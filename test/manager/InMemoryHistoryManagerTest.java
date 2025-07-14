package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    private InMemoryHistoryManager historyManager;
    private Task task;
    private Epic epic;
    private Subtask subtask;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();

        task = new Task("Task", "Description", LocalDateTime.now(), Duration.ofMinutes(20));
        task.setId(1);

        epic = new Epic("Epic", "Epic description");
        epic.setId(2);

        subtask = new Subtask("Subtask", "Subtask description", LocalDateTime.now(),
                Duration.ofMinutes(20), epic.getId());
        subtask.setId(3);
    }

    @Test
    void emptyHistory() {
        assertTrue(historyManager.getHistory().isEmpty(), "История должна быть пустой изначально");
    }

    @Test
    void shouldNotDuplicateTasksInHistory() {
        historyManager.add(task);
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "Задача не должна дублироваться в истории");
    }

    @Test
    void shouldRemoveTaskFromHistory() {
        historyManager.add(task);
        historyManager.remove(task.getId());

        List<Task> history = historyManager.getHistory();
        assertFalse(history.contains(task), "Задача должна удаляться из истории");
    }

    @Test
    void shouldRemoveEpicAndSubtasksFromHistory() {
        historyManager.add(epic);
        historyManager.add(subtask);

        historyManager.remove(epic.getId());
        historyManager.remove(subtask.getId());

        List<Task> history = historyManager.getHistory();
        assertFalse(history.contains(epic), "Эпик должен быть удалён из истории");
        assertFalse(history.contains(subtask), "Подзадача должна быть удалена из истории");
    }

    @Test
    void shouldMaintainCorrectOrderInHistory() {
        historyManager.add(task);
        historyManager.add(epic);
        historyManager.add(subtask);

        List<Task> history = historyManager.getHistory();

        assertEquals(3, history.size(), "История должна содержать три задачи");
        assertEquals(task, history.get(0), "Первая задача в истории должна быть task");
        assertEquals(epic, history.get(1), "Вторая задача в истории должна быть epic");
        assertEquals(subtask, history.get(2), "Третья задача в истории должна быть subtask");
    }

    @Test
    void correctUsageInHistory() {
        historyManager.add(task);
        historyManager.add(epic);
        historyManager.add(subtask);

        historyManager.remove(epic.getId());
        List<Task> history = historyManager.getHistory();
        assertEquals(List.of(task, subtask), history, "После удаления среднего элемента порядок остальных " +
                "должен сохраняться");
    }
}