package manager;

import java.nio.file.Path;

public class Managers {
    public static TaskManager getDefault() {
        return new FileBackedTaskManager(Path.of("tasks.csv"));
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
