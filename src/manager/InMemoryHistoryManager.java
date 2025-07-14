package manager;

import task.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {

    private final Map<Integer, Node> history = new HashMap<>();
    private Node head;
    private Node tail;

    @Override
    public void add(Task task) {
        if (task == null || history.containsKey(task.getId())) return;
        linkLast(task);
    }

    @Override
    public void remove(int id) {
        Node node = history.remove(id);
        removeNode(node);
    }

    @Override
    public List<Task> getHistory() {
        List<Task> taskHistory = new ArrayList<>();
        Node current = head;

        while (current != null) {
            taskHistory.add(current.task);
            current = current.next;
        }
        return taskHistory;
    }

    private void linkLast(Task task) {
        Node node = new Node(task);
        if (tail == null) {
            head = node;
            tail = node;
        } else {
            tail.next = node;
            node.prev = tail;
            tail = node;
        }
        history.put(task.getId(), node);
    }

    private void removeNode(Node node) {
        if (node == null) return;
        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            head = node.next;
        }
        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            tail = node.prev;
        }
        node.prev = null;
        node.next = null;
        node.task = null;
    }

    private static class Node {
        Task task;
        Node prev;
        Node next;

        public Node(Task task) {
            this.task = task;
        }

    }
}