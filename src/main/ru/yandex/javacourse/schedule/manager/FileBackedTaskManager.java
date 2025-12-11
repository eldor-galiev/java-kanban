package main.ru.yandex.javacourse.schedule.manager;

import main.ru.yandex.javacourse.schedule.exception.ManagerSaveException;
import main.ru.yandex.javacourse.schedule.tasks.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final String filePath;

    public FileBackedTaskManager(String filePath) {
        this.filePath = filePath;
        loadFromFile();
    }

    private void loadFromFile() {
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            if (lines.isEmpty()) return;

            int maxId = 0;
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",");

                int id = Integer.parseInt(parts[0]);
                TaskType type = TaskType.valueOf(parts[1]);
                String name = parts[2];
                TaskStatus status = TaskStatus.valueOf(parts[3]);
                String description = parts[4];
                LocalDateTime startTime = null;
                Duration duration = null;
                if (parts.length > 5 && !parts[5].isBlank()) {
                    startTime = LocalDateTime.parse(parts[5]);
                }
                if (parts.length > 6 && !parts[6].isBlank()) {
                    duration = Duration.parse(parts[6]);
                }

                maxId = Math.max(maxId, id);

                switch (type) {
                    case TASK -> {
                        Task task = new Task(id, name, description, status, startTime, duration);
                        super.tasks.put(id, task);
                        if (task.getStartTime() != null) {
                            super.prioritizedTasks.add(task);
                        }
                    }
                    case EPIC -> super.epics.put(id, new Epic(id, name, description));
                    case SUBTASK -> {
                        int epicId = Integer.parseInt(parts[7]);
                        Subtask subtask = new Subtask(id, name, description, status, epicId, startTime, duration);
                        Epic epic = epics.get(epicId);
                        if (epic != null) {
                            super.subtasks.put(id, subtask);
                            epic.addSubtaskId(subtask.getId());
                            updateEpicStatus(epicId);
                            updateEpicTimings(epicId);
                        }
                    }
                }
            }
            super.generatorId = maxId;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void save() {
        ArrayList<Task> allTasks = new ArrayList<>(getTasks());
        allTasks.addAll(getEpics());
        allTasks.addAll(getSubtasks());
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("id,type,name,status,description,start_time,duration,epicId\n");
            for (Task task : allTasks) {
                writer.write(task.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            throw new ManagerSaveException(String.format("Failed to save tasks to file: %s", filePath));
        }
    }

    @Override
    public int addNewTask(Task task) {
        int id = super.addNewTask(task);
        save();
        return id;
    }

    @Override
    public int addNewEpic(Epic epic) {
        int id = super.addNewEpic(epic);
        save();
        return id;
    }

    @Override
    public Integer addNewSubtask(Subtask subtask) {
        Integer id = super.addNewSubtask(subtask);
        save();
        return id;
    }

    public static void main(String[] args) {
        String testFile = "tasks.csv";

        FileBackedTaskManager manager1 = new FileBackedTaskManager(testFile);

        Task task1 = new Task("Задача 1", "Описание задачи 1", TaskStatus.NEW,
                LocalDateTime.now(), Duration.ofMinutes(10));
        manager1.addNewTask(task1);
        Epic epic1 = new Epic("Эпик с подзадачами", "Эпик с 2 подзадачами");
        int epic1Id = manager1.addNewEpic(epic1);
        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи 1", TaskStatus.NEW, epic1Id,
                LocalDateTime.now(), Duration.ofMinutes(10));
        manager1.addNewSubtask(subtask1);
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание подзадачи 2", TaskStatus.IN_PROGRESS,
                epic1Id, LocalDateTime.now().plusHours(2), Duration.ofMinutes(60));
        manager1.addNewSubtask(subtask2);
        Epic epic2 = new Epic("Эпик без подзадач", "Эпик без подзадач");
        manager1.addNewEpic(epic2);

        System.out.println("Первый менеджер содержит:");
        printAllTasks(manager1);
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("Все задачи:");
        System.out.println("- Простые задачи (" + manager.getTasks().size() + "):");
        for (Task task : manager.getPrioritizedTasks()) {
            System.out.println("  [id: " + task.getId() + "] " + task.getName() +
                    " - " + task.getStatus());
        }

        System.out.println("- Эпики (" + manager.getEpics().size() + "):");
        for (Epic epic : manager.getEpics()) {
            System.out.println("  [id: " + epic.getId() + "] " + epic.getName() +
                    " - " + epic.getStatus() + " (подзадач: " +
                    epic.getSubtaskIds().size() + ")");
        }

        System.out.println("- Подзадачи (" + manager.getSubtasks().size() + "):");
        for (Subtask subtask : manager.getSubtasks()) {
            System.out.println("  [id: " + subtask.getId() + "] " + subtask.getName() +
                    " - " + subtask.getStatus() + " (эпик: " +
                    subtask.getEpicId() + ")");
        }
        System.out.println();
    }
}