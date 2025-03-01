package manager;

import task.Task;
import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager {
    private static final int HISTORY_LIMIT = 10;
    private final ArrayList<Task> history = new ArrayList<>();

@Override
    public void add(Task task) {
        for (int i = 0; i < history.size(); i++) {
            if (history.get(i).getId() == task.getId()) {
                history.remove(i);
                break;
            }
        }
        if (history.size() == HISTORY_LIMIT) {
            history.remove(0);
        }

        history.add(task);
    }
    @Override
    public void remove(int id) {
        for (int i = history.size() - 1; i >= 0; i--) {
            if (history.get(i).getId() == id) {
                history.remove(i);
            }
        }
    }
    @Override
    public ArrayList<Task> getHistory() {
        return new ArrayList<>(history);
    }
}