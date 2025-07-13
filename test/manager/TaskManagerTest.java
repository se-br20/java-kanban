package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.Status;
import task.Subtask;
import task.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;

    public abstract T createTaskManager();

    @BeforeEach
    void setUp() {
        taskManager = createTaskManager();
    }

    @Test
    void shouldStartEmpty() {
        assertTrue(taskManager.getTasks().isEmpty(), "Tasks должны быть пусты");
        assertTrue(taskManager.getEpics().isEmpty(), "Epics должны быть пусты");
        assertTrue(taskManager.getSubtasks().isEmpty(), "Subtasks должны быть пусты");
    }

    @Test
    void shouldAddAndGetTasks() {
        Task task = new Task("Task", "Description", LocalDateTime.now(), Duration.ofMinutes(15));
        taskManager.addTask(task);
        Task received = taskManager.getTaskById(task.getId());
        assertEquals(task, received, "Добавленная и полученная задачи должны совпадать");
    }

    @Test
    void shouldAddEpicAndSubtasksAndFindStatusAndTime() {
        Epic epic = new Epic("Epic", "Description");
        taskManager.addEpic(epic);
        Subtask subtask1 = new Subtask("Subtask 1", "Description", LocalDateTime.now(),
                Duration.ofMinutes(10), epic.getId());
        Subtask subtask2 = new Subtask("Subtask 2", "Description", LocalDateTime.now().plusMinutes(20),
                Duration.ofMinutes(30), epic.getId());
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        subtask1.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask1);
        assertEquals(Status.IN_PROGRESS, taskManager.getEpicById(epic.getId()).getStatus(), "Если часть " +
                "подзадач выполнена, эпик должен быть IN_PROGRESS");

        Epic received = taskManager.getEpicById(epic.getId());
        assertEquals(subtask1.getStartTime(), received.getStartTime(), "Время старта эпика должно быть " +
                "временем старта самой ранней подзадачи");
        assertEquals(subtask2.getStartTime(), received.getEndTime(), "Время окончания эпика должно быть " +
                "временем окончания самой поздней подзадачи");
        assertEquals(Duration.ofMinutes(40), received.getDuration(), "Длительность эпика должна равняться " +
                "сумме длительностей подзадач");
    }

    @Test
    void sholdRemoveTaskAndEpic() {
        Task task = new Task("Task", "Description", LocalDateTime.now(), Duration.ofMinutes(15));
        taskManager.addTask(task);
        taskManager.removeTaskById(task.getId());
        assertNull(taskManager.getTaskById(task.getId()), "Task должен быть удалён");

        Epic epic = new Epic("Epic", "Description");
        taskManager.addEpic(epic);
        Subtask subtask = new Subtask("Subtask", "Description", LocalDateTime.now(),
                Duration.ofMinutes(15), epic.getId());
        taskManager.addSubtask(subtask);
        taskManager.removeEpicById(epic.getId());
        assertNull(taskManager.getEpicById(epic.getId()), "Epic должен быть удалён");
        assertNull(taskManager.getSubtaskById(subtask.getId()), "Subtask должен быть удалён вместе с Epic");
    }

    @Test
    void getHistoryTracksViewsWithoutDuplicates() {
        Task task1 = new Task("Task 1", "Description", LocalDateTime.now(), Duration.ofMinutes(15));
        Task task2 = new Task("Task 2", "Description", LocalDateTime.now().plusMinutes(11),
                Duration.ofMinutes(15));
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        taskManager.getTaskById(task1.getId());
        taskManager.getTaskById(task2.getId());
        taskManager.getTaskById(task1.getId());

        List<Task> history = taskManager.getHistory();
        assertEquals(List.of(task1,task2), history);
    }

    @Test
    void getPrioritizedTasksSortedByStartTime(){
        Task task1 = new Task("Task 1", "Description",
                LocalDateTime.of(2025,1,1,10,0), Duration.ofMinutes(10));
        Task task2 = new Task("Task 2", "Description",
                LocalDateTime.of(2025,1,1,7,0), Duration.ofMinutes(10));

        taskManager.addTask(task1);
        taskManager.addTask(task2);

        List<Task> prioritized = taskManager.getPrioritizedTasks();
        assertEquals(List.of(task2, task1), prioritized);
    }

    @Test
    void shouldDetectOverlapAndTrow(){
        Task task1 = new Task("Task 1", "Description",
                LocalDateTime.of(2025,1,1,10,0), Duration.ofMinutes(60));
        Task task2 = new Task("Task 2", "Description",
                LocalDateTime.of(2025,1,1,10,30), Duration.ofMinutes(30));
        taskManager.addTask(task1);
        assertThrows(IllegalArgumentException.class, () -> taskManager.addTask(task2), "Пересекающиеся задачи");

    }

}
