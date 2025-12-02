package main.ru.yandex.javacourse.schedule.manager;

import main.ru.yandex.javacourse.schedule.tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private Node head;
    private Node tail;
    private final HashMap<Integer, Node> nodeMap = new HashMap<>();

    @Override
    public List<Task> getHistory() {
        List<Task> history = new ArrayList<>();
        Node current = head;
        while (current != null) {
            history.add(current.item);
            current = current.next;
        }
        return List.copyOf(history);
    }

	@Override
	public void addTask(Task task) {
		if (task == null) {
			return;
		}
        removeNode(nodeMap.remove(task.getId()));
        addNode(task);
    }

    @Override
    public void remove(int id) {
        removeNode(nodeMap.remove(id));
    }

    private void addNode(Task task) {
        final Node prev = tail;
        final Node newNode = new Node(prev, task, null);
        tail = newNode;
        if (prev == null)
            head = newNode;
        else
            prev.next = newNode;

        nodeMap.put(task.getId(), newNode);
    }

    private void removeNode(Node removingNode) {
        if (removingNode == null) return;

        final Node next = removingNode.next;
        final Node prev = removingNode.prev;

        if (prev == null) {
            head = next;
        } else {
            prev.next = next;
            removingNode.prev = null;
        }

        if (next == null) {
            tail = prev;
        } else {
            next.prev = prev;
            removingNode.next = null;
        }

        removingNode.item = null;
    }

}
