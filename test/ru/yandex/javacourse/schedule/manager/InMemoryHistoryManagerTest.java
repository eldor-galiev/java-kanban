package test.ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import main.ru.yandex.javacourse.schedule.manager.HistoryManager;
import main.ru.yandex.javacourse.schedule.manager.Managers;
import main.ru.yandex.javacourse.schedule.tasks.Task;
import main.ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryHistoryManagerTest {

    HistoryManager historyManager;

    @BeforeEach
    public void initHistoryManager() {
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    public void testAddTaskAndGetHistory() {
        Task task = new Task("Test 1", "Testing task 1", TaskStatus.NEW);
        task.setId(1);

        historyManager.addTask(task);
        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size(), "History should contain 1 task");
        assertEquals(task, history.getFirst(), "Task in history should match the added task");
    }

    @Test
    public void testAddDuplicateTaskShouldReplaceOldVersion() {
        Task task = new Task("Test 1", "Testing task 1", TaskStatus.NEW);
        task.setId(1);

        historyManager.addTask(task);
        task.setStatus(TaskStatus.IN_PROGRESS);
        historyManager.addTask(task);

        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size(), "History should still contain only 1 task");
        assertEquals(TaskStatus.IN_PROGRESS, history.getFirst().getStatus(),
                "Task in history should have updated status");
    }

    @Test
    public void testHistoryOrder() {
        Task task1 = new Task("Test 1", "Testing task 1", TaskStatus.NEW);
        task1.setId(1);
        Task task2 = new Task("Test 2", "Testing task 2", TaskStatus.NEW);
        task2.setId(2);
        Task task3 = new Task("Test 3", "Testing task 3", TaskStatus.NEW);
        task3.setId(3);

        historyManager.addTask(task1);
        historyManager.addTask(task2);
        historyManager.addTask(task3);

        List<Task> history = historyManager.getHistory();

        assertEquals(3, history.size(), "History should contain 3 tasks");
        assertEquals(task1, history.get(0), "First task should be task1");
        assertEquals(task2, history.get(1), "Second task should be task2");
        assertEquals(task3, history.get(2), "Third task should be task3");
    }

    @Test
    public void testRemoveTaskFromHistory() {
        Task task1 = new Task("Test 1", "Testing task 1", TaskStatus.NEW);
        task1.setId(1);
        Task task2 = new Task("Test 2", "Testing task 2", TaskStatus.NEW);
        task2.setId(2);
        Task task3 = new Task("Test 3", "Testing task 3", TaskStatus.NEW);
        task3.setId(3);

        historyManager.addTask(task1);
        historyManager.addTask(task2);
        historyManager.addTask(task3);

        historyManager.remove(2);

        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size(), "History should contain 2 tasks after removal");
        assertEquals(task1, history.get(0), "First task should be task1");
        assertEquals(task3, history.get(1), "Second task should be task3");
    }

    @Test
    public void testRemoveFirstTaskFromHistory() {
        Task task1 = new Task("Test 1", "Testing task 1", TaskStatus.NEW);
        task1.setId(1);
        Task task2 = new Task("Test 2", "Testing task 2", TaskStatus.NEW);
        task2.setId(2);

        historyManager.addTask(task1);
        historyManager.addTask(task2);

        historyManager.remove(1);

        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size(), "History should contain 1 task");
        assertEquals(task2, history.getFirst(), "Remaining task should be task2");
    }

    @Test
    public void testRemoveLastTaskFromHistory() {
        Task task1 = new Task("Test 1", "Testing task 1", TaskStatus.NEW);
        task1.setId(1);
        Task task2 = new Task("Test 2", "Testing task 2", TaskStatus.NEW);
        task2.setId(2);

        historyManager.addTask(task1);
        historyManager.addTask(task2);

        historyManager.remove(2);

        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size(), "History should contain 1 task");
        assertEquals(task1, history.getFirst(), "Remaining task should be task1");
    }

    @Test
    public void testRemoveNonExistentTask() {
        Task task = new Task("Test 1", "Testing task 1", TaskStatus.NEW);
        task.setId(1);

        historyManager.addTask(task);

        historyManager.remove(999);

        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size(), "History should still contain 1 task");
    }

    @Test
    public void testAddNullTask() {
        historyManager.addTask(null);

        List<Task> history = historyManager.getHistory();

        assertEquals(0, history.size(), "History should be empty when adding null task");
    }

    @Test
    public void testTaskUpdatesAndHistoryPreservesLastVersion() {
        Task task = new Task("Test", "Testing task", TaskStatus.NEW);
        task.setId(1);

        historyManager.addTask(task);

        task.setStatus(TaskStatus.IN_PROGRESS);
        historyManager.addTask(task);

        task.setStatus(TaskStatus.DONE);
        historyManager.addTask(task);

        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size(), "History should contain only 1 task (last version)");
        assertEquals(TaskStatus.DONE, history.getFirst().getStatus(),
                "History should contain the last version of the task");
    }
}