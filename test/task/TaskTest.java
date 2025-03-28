package task;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void tasksWithSameIdShouldBeEqual() {
        Task task1 = new Task("Task 1", "Description");
        Task task2 = new Task("Task 2", "Another description");
        task1.setId(1);
        task2.setId(1);

        assertEquals(task1, task2, "Задачи с одинаковым ID должны быть равны");
        assertEquals(task1.hashCode(), task2.hashCode(), "Задачи с одинаковым ID должны иметь одинаковый hashCode");
    }

    @Test
    void tasksWithDifferentIdsShouldNotBeEqual() {
        Task task1 = new Task("Task 1", "Description");
        Task task2 = new Task("Task 2", "Another description");
        task1.setId(1);
        task2.setId(2);

        assertNotEquals(task1, task2, "Задачи с разными ID не должны быть равны");
    }

    @Test
    void epicAndSubtaskWithSameIdShouldNotBeEqual() {
        Epic epic = new Epic("Epic", "Epic description");
        Subtask subtask = new Subtask("Subtask", "Subtask description", 1);
        epic.setId(2);
        subtask.setId(2);

        assertNotEquals(epic, subtask, "Объекты Epic и Subtask с одинаковым ID не должны быть равны");
    }

    @Test
    void epicShouldNotContainItselfAsSubtask() {
        Epic epic = new Epic("Epic", "Epic description");
        epic.setId(1);

        assertFalse(epic.getSubtaskIds().contains(1), "Эпик не должен содержать сам себя в подзадачах");
    }

    @Test
    void subtaskShouldNotBeItsOwnEpic() {
        Subtask subtask = new Subtask("Subtask", "Description", 1); // EpicId = 1
        subtask.setId(2); // ID = 2, а EpicId = 1 (корректно)

        assertNotEquals(subtask.getId(), subtask.getEpicId(), "Подзадача не может быть своим же эпиком");
    }

    @Test
    void tasksShouldHaveUniqueIds() {
        Task task1 = new Task("Task 1", "Description");
        Task task2 = new Task("Task 2", "Another description");

        task1.setId(1);
        task2.setId(2);

        assertNotEquals(task1.getId(), task2.getId(), "ID задач должны быть уникальными");
    }

    @Test
    void shouldChangeTaskStatusCorrectly() {
        Task task = new Task("Task", "Description");
        task.setStatus(Status.NEW);

        assertEquals(Status.NEW, task.getStatus(), "Статус задачи должен быть NEW");

        task.setStatus(Status.IN_PROGRESS);
        assertEquals(Status.IN_PROGRESS, task.getStatus(), "Статус задачи должен измениться на IN_PROGRESS");

        task.setStatus(Status.DONE);
        assertEquals(Status.DONE, task.getStatus(), "Статус задачи должен измениться на DONE");
    }
}