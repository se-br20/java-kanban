package manager;

import task.Node;
import task.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {


    private final Map<Integer, Node> history = new HashMap<>();
    private Node head;
    private Node tail;

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }
        remove(task.getId());
        linkLast(task);
    }

    @Override
    public void remove(int id) {
        Node node = history.remove(id);
        if (node != null) {
            removeNode(node);
        }

    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    private void linkLast(Task task) {
        Node node = new Node(task);
        if (tail == null) {
            head = node;
            tail = node;
        } else {
            tail.setNext(node);
            node.setPrev(tail);
            tail = node;
        }
        history.put(task.getId(), node);
    }

    private void removeNode(Node node) {
        if (node == null) return;
        if (node.getPrev() != null) {
            node.getPrev().setNext(node.getNext());
        } else {
            head = node.getNext();
        }
        if (node.getNext() != null) {
            node.getNext().setPrev(node.getPrev());
        } else {
            tail = node.getPrev();
        }
        node.setPrev(null);
        node.setNext(null);
    }

    private List<Task> getTasks() {
        List<Task> taskHistory = new ArrayList<>();
        Node current = head;

        while (current != null) {
            taskHistory.add(current.getTask());
            current = current.getNext();
        }
        return taskHistory;
    }
}