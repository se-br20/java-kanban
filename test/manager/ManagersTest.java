package manager;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    @Test
    void shouldReturnInitializedTaskManager() {
        TaskManager taskManager = Managers.getDefault();
        assertNotNull(taskManager, "Менеджер задач должен быть проинициализирован");
    }

    @Test
    void shouldReturnInitializedHistoryManager() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        assertNotNull(historyManager, "Менеджер истории должен быть проинициализирован");
    }
}
