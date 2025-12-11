package test.ru.yandex.javacourse.schedule.manager;

import main.ru.yandex.javacourse.schedule.manager.HistoryManager;
import main.ru.yandex.javacourse.schedule.manager.TaskManager;
import main.ru.yandex.javacourse.schedule.tasks.Epic;
import main.ru.yandex.javacourse.schedule.tasks.Subtask;
import main.ru.yandex.javacourse.schedule.tasks.Task;
import main.ru.yandex.javacourse.schedule.tasks.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    
    protected T manager;
    
    protected abstract T createManager();
    
    @BeforeEach
    public void initManager() {
        manager = createManager();
    }
    
    @Test
    public void testAddTask() {
        Task task = new Task("Test 1", "Testing task 1", TaskStatus.NEW);
        manager.addNewTask(task);
        assertEquals(1, manager.getTasks().size(), "task should be added");
        Task addedTask = manager.getTasks().getFirst();
        assertEquals(task, addedTask, "added task id should be set");
        Task byIdTask = manager.getTask(task.getId());
        assertEquals(task, byIdTask, "added task id should be found");
    }
    
    @Test
    public void testAddTaskWithId() {
        Task task = new Task("Test 1", "Testing task 1", TaskStatus.NEW);
        manager.addNewTask(task);
        Task addedTask = manager.getTasks().getFirst();
        assertEquals(task.getId(), addedTask.getId(), "task should have auto-generated id");
    }
    
    @Test
    public void testAddTaskWithAndWithoutId() {
        Task task0 = new Task("Test 1", "Testing task 1", TaskStatus.NEW);
        Task task1 = new Task("Test 2", "Testing task 2", TaskStatus.NEW);
        manager.addNewTask(task0);
        manager.addNewTask(task1);
        assertEquals(2, manager.getTasks().size(), "both tasks should be added");
        assertEquals(1, task0.getId(), "first task should have id 1");
        assertEquals(2, task1.getId(), "second task should have id 2");
    }
    
    @Test
    public void checkTaskNotChangedAfterAddTask() {
        String name = "Test 1";
        String description = "Testing task 1";
        TaskStatus status = TaskStatus.NEW;
        Task task1before = new Task(name, description, status);
        manager.addNewTask(task1before);
        Task task1after = manager.getTask(task1before.getId());
        assertEquals(description, task1after.getDescription());
        assertEquals(status, task1after.getStatus());
        assertEquals(name, task1after.getName());
    }
    
    @Test
    public void testEpicCreation() {
        Epic epic = new Epic("Epic 1", "Test epic");
        int epicId = manager.addNewEpic(epic);
        
        assertNotEquals(0, epicId, "Epic should have non-zero id");
        assertEquals(1, manager.getEpics().size(), "Epic should be added");
        assertEquals(TaskStatus.NEW, epic.getStatus(), "New epic should have NEW status");
        assertTrue(epic.getSubtaskIds().isEmpty(), "New epic should have empty subtask list");
    }
    
    @Test
    public void testSubtaskCreation() {
        Epic epic = new Epic("Epic 1", "Test epic");
        int epicId = manager.addNewEpic(epic);
        
        Subtask subtask = new Subtask("Subtask 1", "Test subtask", TaskStatus.NEW, epicId);
        Integer subtaskId = manager.addNewSubtask(subtask);
        
        assertNotNull(subtaskId, "Subtask should be added");
        assertEquals(1, manager.getSubtasks().size(), "Subtask should be added to manager");
        assertEquals(1, epic.getSubtaskIds().size(), "Epic should contain subtask id");
        assertTrue(epic.getSubtaskIds().contains(subtaskId), "Epic should contain the subtask id");
    }
    
    @Test
    public void testSubtaskCreationWithInvalidEpic() {
        Subtask subtask = new Subtask("Subtask 1", "Test subtask", TaskStatus.NEW, 999);
        Integer subtaskId = manager.addNewSubtask(subtask);
        
        assertNull(subtaskId, "Subtask with invalid epic should not be added");
        assertEquals(0, manager.getSubtasks().size(), "No subtasks should be added");
    }
    
    @Test
    public void testDeleteSubtaskRemovesFromEpic() {
        Epic epic = new Epic("Epic 1", "Test epic");
        int epicId = manager.addNewEpic(epic);
        
        Subtask subtask = new Subtask("Subtask 1", "Test subtask", TaskStatus.NEW, epicId);
        Integer subtaskId = manager.addNewSubtask(subtask);
        
        assertNotNull(subtaskId);
        assertEquals(1, epic.getSubtaskIds().size(), "Epic should contain subtask before deletion");
        
        manager.deleteSubtask(subtaskId);
        
        assertEquals(0, manager.getSubtasks().size(), "Subtask should be removed from manager");
        assertEquals(0, epic.getSubtaskIds().size(), "Epic should not contain deleted subtask id");
        assertFalse(epic.getSubtaskIds().contains(subtaskId), "Epic should not contain removed subtask id");
    }
    
    @Test
    public void testDeleteEpicRemovesSubtasks() {
        Epic epic = new Epic("Epic 1", "Test epic");
        int epicId = manager.addNewEpic(epic);
        
        Subtask subtask1 = new Subtask("Subtask 1", "Test subtask 1", TaskStatus.NEW, epicId);
        Subtask subtask2 = new Subtask("Subtask 2", "Test subtask 2", TaskStatus.NEW, epicId);
        
        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);
        
        assertEquals(2, manager.getSubtasks().size(), "Two subtasks should be added");
        
        manager.deleteEpic(epicId);
        
        assertEquals(0, manager.getEpics().size(), "Epic should be removed");
        assertEquals(0, manager.getSubtasks().size(), "All subtasks should be removed");
    }
    
    @Test
    public void testEpicStatusCalculationAllNew() {
        Epic epic = new Epic("Epic 1", "Test epic");
        int epicId = manager.addNewEpic(epic);
        
        Subtask subtask1 = new Subtask("Subtask 1", "Test", TaskStatus.NEW, epicId);
        Subtask subtask2 = new Subtask("Subtask 2", "Test", TaskStatus.NEW, epicId);
        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);
        
        assertEquals(TaskStatus.NEW, epic.getStatus(), "Epic with all NEW subtasks should be NEW");
    }
    
    @Test
    public void testEpicStatusCalculationAllDone() {
        Epic epic = new Epic("Epic 1", "Test epic");
        int epicId = manager.addNewEpic(epic);
        
        Subtask subtask1 = new Subtask("Subtask 1", "Test", TaskStatus.NEW, epicId);
        Subtask subtask2 = new Subtask("Subtask 2", "Test", TaskStatus.NEW, epicId);
        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);
        
        subtask1.setStatus(TaskStatus.DONE);
        subtask2.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask1);
        manager.updateSubtask(subtask2);
        
        assertEquals(TaskStatus.DONE, epic.getStatus(), "Epic with all DONE subtasks should be DONE");
    }
    
    @Test
    public void testEpicStatusCalculationNewAndDone() {
        Epic epic = new Epic("Epic 1", "Test epic");
        int epicId = manager.addNewEpic(epic);
        
        Subtask subtask1 = new Subtask("Subtask 1", "Test", TaskStatus.NEW, epicId);
        Subtask subtask2 = new Subtask("Subtask 2", "Test", TaskStatus.NEW, epicId);
        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);
        
        subtask1.setStatus(TaskStatus.NEW);
        subtask2.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask1);
        manager.updateSubtask(subtask2);
        
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus(),
                "Epic with mixed NEW and DONE subtasks should be IN_PROGRESS");
    }
    
    @Test
    public void testEpicStatusCalculationAllInProgress() {
        Epic epic = new Epic("Epic 1", "Test epic");
        int epicId = manager.addNewEpic(epic);
        
        Subtask subtask1 = new Subtask("Subtask 1", "Test", TaskStatus.NEW, epicId);
        Subtask subtask2 = new Subtask("Subtask 2", "Test", TaskStatus.NEW, epicId);
        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);
        
        subtask1.setStatus(TaskStatus.IN_PROGRESS);
        subtask2.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubtask(subtask1);
        manager.updateSubtask(subtask2);
        
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus(),
                "Epic with all IN_PROGRESS subtasks should be IN_PROGRESS");
    }
    
    @Test
    public void testTaskModificationViaSettersAffectsManager() {
        Task task = new Task("Original", "Original description", TaskStatus.NEW);
        int taskId = manager.addNewTask(task);
        
        task.setName("Modified");
        task.setDescription("Modified description");
        task.setStatus(TaskStatus.IN_PROGRESS);
        
        Task retrievedTask = manager.getTask(taskId);
        
        assertEquals("Modified", retrievedTask.getName(),
                "Task in manager is affected by setter changes");
        assertEquals("Modified description", retrievedTask.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, retrievedTask.getStatus());
    }
    
    @Test
    public void testSubtaskModificationViaSettersBreaksEpicIntegrity() {
        Epic epic = new Epic("Epic 1", "Test epic");
        int epicId = manager.addNewEpic(epic);
        
        Subtask subtask = new Subtask("Original", "Original", TaskStatus.NEW, epicId);
        manager.addNewSubtask(subtask);
        
        subtask.setStatus(TaskStatus.DONE);
        
        assertEquals(TaskStatus.NEW, epic.getStatus(),
                "Epic status should not change without updateSubtask call");
        
        manager.updateSubtask(subtask);
        assertEquals(TaskStatus.DONE, epic.getStatus(),
                "Epic status should update after updateSubtask");
    }
    
    @Test
    public void testDeleteAllSubtasksClearsEpicLists() {
        Epic epic1 = new Epic("Epic 1", "Test epic 1");
        Epic epic2 = new Epic("Epic 2", "Test epic 2");
        int epicId1 = manager.addNewEpic(epic1);
        int epicId2 = manager.addNewEpic(epic2);
        
        Subtask subtask1 = new Subtask("Subtask 1", "Test", TaskStatus.NEW, epicId1);
        Subtask subtask2 = new Subtask("Subtask 2", "Test", TaskStatus.NEW, epicId2);
        
        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);
        
        assertEquals(1, epic1.getSubtaskIds().size());
        assertEquals(1, epic2.getSubtaskIds().size());
        
        manager.deleteSubtasks();
        
        assertEquals(0, manager.getSubtasks().size(), "All subtasks should be deleted");
        assertEquals(0, epic1.getSubtaskIds().size(), "Epic 1 should have empty subtask list");
        assertEquals(0, epic2.getSubtaskIds().size(), "Epic 2 should have empty subtask list");
        assertEquals(TaskStatus.NEW, epic1.getStatus(), "Empty epic should have NEW status");
        assertEquals(TaskStatus.NEW, epic2.getStatus(), "Empty epic should have NEW status");
    }
    
    @Test
    public void testUpdateEpicDoesNotAffectSubtasks() {
        Epic epic = new Epic("Original", "Original description");
        int epicId = manager.addNewEpic(epic);
        
        Subtask subtask = new Subtask("Subtask", "Test", TaskStatus.NEW, epicId);
        manager.addNewSubtask(subtask);
        
        Epic updatedEpic = new Epic("Updated", "Updated description");
        updatedEpic.setId(epicId);
        
        manager.updateEpic(updatedEpic);
        
        Epic retrievedEpic = manager.getEpic(epicId);
        assertEquals("Updated", retrievedEpic.getName());
        assertEquals("Updated description", retrievedEpic.getDescription());
        assertEquals(1, retrievedEpic.getSubtaskIds().size(),
                "Epic should still contain subtask after update");
    }
    
    @Test
    public void testHistoryAfterDeletion() {
        Task task = new Task("Task 1", "Test", TaskStatus.NEW);
        Epic epic = new Epic("Epic 1", "Test");
        int taskId = manager.addNewTask(task);
        int epicId = manager.addNewEpic(epic);
        
        Subtask subtask = new Subtask("Subtask 1", "Test", TaskStatus.NEW, epicId);
        Integer subtaskId = manager.addNewSubtask(subtask);
        
        manager.getTask(taskId);
        manager.getEpic(epicId);
        manager.getSubtask(subtaskId);
        
        List<Task> history = manager.getHistory();
        assertEquals(3, history.size(), "History should contain 3 items");
        
        manager.deleteSubtask(subtaskId);
        
        history = manager.getHistory();
        assertEquals(2, history.size(), "History should not contain deleted subtask");
        
        manager.deleteEpic(epicId);
        
        history = manager.getHistory();
        assertEquals(1, history.size(), "History should only contain task");
        assertEquals(taskId, history.getFirst().getId());
    }
    
    @Test
    public void testTaskEqualityAndIds() {
        Task task1 = new Task("Task", "Description", TaskStatus.NEW);
        Task task2 = new Task("Task", "Description", TaskStatus.NEW);
        
        manager.addNewTask(task1);
        manager.addNewTask(task2);
        
        assertNotEquals(task1.getId(), task2.getId(), "Tasks should have different ids");
        assertNotEquals(task1, task2, "Tasks with different ids should not be equal");
        
        Task task1Copy = new Task(task1.getId(), "Task", "Description", TaskStatus.NEW);
        assertEquals(task1, task1Copy, "Tasks with same id should be equal");
    }
    
    @Test
    public void testGetEpicSubtasksReturnsCorrectList() {
        Epic epic = new Epic("Epic", "Test");
        int epicId = manager.addNewEpic(epic);
        
        Subtask subtask1 = new Subtask("Subtask 1", "Test", TaskStatus.NEW, epicId);
        Subtask subtask2 = new Subtask("Subtask 2", "Test", TaskStatus.NEW, epicId);
        
        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);
        
        List<Subtask> epicSubtasks = manager.getEpicSubtasks(epicId);
        
        assertNotNull(epicSubtasks);
        assertEquals(2, epicSubtasks.size());
        assertTrue(epicSubtasks.contains(subtask1));
        assertTrue(epicSubtasks.contains(subtask2));
    }
    
    @Test
    public void testGetEpicSubtasksForInvalidEpic() {
        List<Subtask> subtasks = manager.getEpicSubtasks(999);
        assertNull(subtasks, "Should return null for invalid epic id");
    }
    
    @Test
    public void testSubtaskHasLinkedEpic() {
        Epic epic = new Epic("Epic", "Test");
        int epicId = manager.addNewEpic(epic);
        
        Subtask subtask = new Subtask("Subtask", "Test", TaskStatus.NEW, epicId);
        Integer subtaskId = manager.addNewSubtask(subtask);
        
        assertNotNull(subtaskId);
        Subtask retrievedSubtask = manager.getSubtask(subtaskId);
        assertEquals(epicId, retrievedSubtask.getEpicId(), "Subtask should have linked epic id");
        
        Epic retrievedEpic = manager.getEpic(epicId);
        assertTrue(retrievedEpic.getSubtaskIds().contains(subtaskId), 
                "Epic should contain subtask id");
    }
    
    @Test
    public void testTaskOverlapping() {
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW,
                LocalDateTime.of(2024, 1, 1, 10, 0), Duration.ofMinutes(60));
        
        Task task2 = new Task("Task 2", "Description 2", TaskStatus.NEW,
                LocalDateTime.of(2024, 1, 1, 10, 30), Duration.ofMinutes(60));
        
        manager.addNewTask(task1);
        
        assertTrue(manager.hasOverlapWithAnyTask(task2), 
                "Tasks with overlapping intervals should be detected");
        
        Task task3 = new Task("Task 3", "Description 3", TaskStatus.NEW,
                LocalDateTime.of(2024, 1, 1, 12, 0), Duration.ofMinutes(60));
        
        assertFalse(manager.hasOverlapWithAnyTask(task3),
                "Tasks with non-overlapping intervals should not be detected");
    }
    
    @Test
    public void testTaskOverlappingWithSubtask() {
        Epic epic = new Epic("Epic", "Test");
        int epicId = manager.addNewEpic(epic);
        
        Subtask subtask1 = new Subtask("Subtask 1", "Test", TaskStatus.NEW, epicId,
                LocalDateTime.of(2024, 1, 1, 10, 0), Duration.ofMinutes(60));
        
        manager.addNewSubtask(subtask1);
        
        Subtask subtask2 = new Subtask("Subtask 2", "Test", TaskStatus.NEW, epicId,
                LocalDateTime.of(2024, 1, 1, 10, 30), Duration.ofMinutes(60));
        
        assertTrue(manager.hasOverlapWithAnyTask(subtask2),
                "Subtasks with overlapping intervals should be detected");
    }
}