package test.ru.yandex.javacourse.schedule.manager;

import main.ru.yandex.javacourse.schedule.manager.FileBackedTaskManager;
import main.ru.yandex.javacourse.schedule.manager.TaskManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest extends TaskManagerTest<TaskManager> {
    
    @TempDir
    File tempDir;
    
    @Override
    protected TaskManager createManager() {
        try {
            Path tempFile = Files.createTempFile(tempDir.toPath(), "tasks", ".csv");
            return new FileBackedTaskManager(tempFile.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Test
    public void testSaveAndLoadEmptyManager() throws IOException {
        Path tempFile = Files.createTempFile(tempDir.toPath(), "tasks", ".csv");
        FileBackedTaskManager manager1 = new FileBackedTaskManager(tempFile.toString());
        
        manager1.save();
        
        FileBackedTaskManager manager2 = new FileBackedTaskManager(tempFile.toString());
        
        assertEquals(0, manager2.getTasks().size());
        assertEquals(0, manager2.getEpics().size());
        assertEquals(0, manager2.getSubtasks().size());
    }
    
    @Test
    public void testSaveAndLoadWithTasks() throws IOException {
        Path tempFile = Files.createTempFile(tempDir.toPath(), "tasks", ".csv");
        FileBackedTaskManager manager1 = new FileBackedTaskManager(tempFile.toString());
        
        main.ru.yandex.javacourse.schedule.tasks.Task task = new main.ru.yandex.javacourse.schedule.tasks.Task(
                "Task", "Description", main.ru.yandex.javacourse.schedule.tasks.TaskStatus.NEW);
        manager1.addNewTask(task);
        
        FileBackedTaskManager manager2 = new FileBackedTaskManager(tempFile.toString());
        
        assertEquals(1, manager2.getTasks().size());
        assertEquals("Task", manager2.getTasks().getFirst().getName());
    }
    
    @Test
    public void testSaveAndLoadWithEpicAndSubtasks() throws IOException {
        Path tempFile = Files.createTempFile(tempDir.toPath(), "tasks", ".csv");
        FileBackedTaskManager manager1 = new FileBackedTaskManager(tempFile.toString());
        
        main.ru.yandex.javacourse.schedule.tasks.Epic epic = new main.ru.yandex.javacourse.schedule.tasks.Epic(
                "Epic", "Description");
        int epicId = manager1.addNewEpic(epic);
        
        main.ru.yandex.javacourse.schedule.tasks.Subtask subtask = new main.ru.yandex.javacourse.schedule.tasks.Subtask(
                "Subtask", "Description", main.ru.yandex.javacourse.schedule.tasks.TaskStatus.NEW, epicId);
        manager1.addNewSubtask(subtask);
        
        FileBackedTaskManager manager2 = new FileBackedTaskManager(tempFile.toString());
        
        assertEquals(1, manager2.getEpics().size());
        assertEquals(1, manager2.getSubtasks().size());
        assertEquals(epicId, manager2.getSubtasks().getFirst().getEpicId());
    }
    
    @Test
    public void testExceptionOnInvalidFile() {
        assertThrows(RuntimeException.class, () -> {
            new FileBackedTaskManager("nonexistent/path/tasks.csv");
        }, "Should throw exception when file cannot be loaded");
    }
}