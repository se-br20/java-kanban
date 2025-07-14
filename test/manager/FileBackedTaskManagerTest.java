package manager;

import org.junit.jupiter.api.BeforeAll;

import java.io.File;
import java.io.IOException;

public class FileBackedTaskManagerTest
        extends TaskManagerTest<FileBackedTaskManager> {
    private static File tempFile;

    @BeforeAll
    static void setup() throws IOException {
        tempFile = File.createTempFile("test", ".csv");
        tempFile.deleteOnExit();
    }

    @Override
    public FileBackedTaskManager createTaskManager() {
        return new FileBackedTaskManager(tempFile.toPath());
    }
}