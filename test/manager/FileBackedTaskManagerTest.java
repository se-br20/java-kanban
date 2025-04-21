package manager;

import org.junit.jupiter.api.*;
import task.*;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class FileBackedTaskManagerTest {

    private File tempFile;
    private FileBackedTaskManager manager;

    @BeforeEach
    void setup() throws IOException {
        tempFile = File.createTempFile("test", ".csv");
        tempFile.deleteOnExit();
        manager = new FileBackedTaskManager(new InMemoryHistoryManager(), tempFile.toPath());
    }

    @Test
    void sholdSaveAndLoadEmptyFile() {
        FileBackedTaskManager loadManager = FileBackedTaskManager.loadFromFile(tempFile);
        assertTrue(loadManager.getTasks().isEmpty(), "Tasks пустые");
        assertTrue(loadManager.getEpics().isEmpty(), "Epics пустые");
        assertTrue(loadManager.getSubtasks().isEmpty(), "Subtasks пустые");
        assertTrue(loadManager.getHistory().isEmpty(), "History пустая");
    }

    @Test
    void testManagerOperations() {
        Task task = new Task("Task", "TaskDescription");
        task.setStatus(Status.NEW);
        manager.addTask(task);

        Epic epic = new Epic("Epic", "EpicDescription");
        manager.addEpic(epic);

        Subtask subtask = new Subtask("Subtusk", "SubtaskDescription", epic.getId());
        subtask.setStatus(Status.IN_PROGRESS);
        manager.addSubtask(subtask);
        assertEquals(1, manager.getTasks().size());
        assertEquals(1, manager.getEpics().size());
        assertEquals(1, manager.getSubtasks().size());

        FileBackedTaskManager loadManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(1, loadManager.getTasks().size());
        assertEquals(1, loadManager.getEpics().size());
        assertEquals(1, loadManager.getSubtasks().size());
        assertEquals(1, loadManager.getHistory().size());
    }


}
