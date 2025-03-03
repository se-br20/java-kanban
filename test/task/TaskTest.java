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
    }

    @Test
    void epicAndSubtaskWithSameIdShouldBeEqual() {
        Epic epic = new Epic("Epic", "Epic description");
        Subtask subtask = new Subtask("Subtask", "Subtask description", 1);
        epic.setId(2);
        subtask.setId(2);

        assertEquals(epic, subtask, "Объекты Epic и Subtask с одинаковым ID должны быть равны");
    }

    @Test
    void epicShouldNotContainItselfAsSubtask() {
        Epic epic = new Epic("Epic", "Epic description");
        epic.setId(1);

        assertFalse(epic.getSubtaskIds().contains(1), "Эпик не должен содержать сам себя в подзадачах");
    }

    @Test
    void subtaskShouldNotBeItsOwnEpic() {
        Subtask subtask = new Subtask("Subtask", "Description", 2);
        subtask.setId(2);

        assertNotEquals(subtask.getId(), subtask.getEpicId(), "Подзадача не может быть своим же эпиком");
    }
}
