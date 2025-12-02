package main.ru.yandex.javacourse.schedule.manager;

import main.ru.yandex.javacourse.schedule.tasks.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
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

            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",");

                int id = Integer.parseInt(parts[0]);
                TaskType type = TaskType.valueOf(parts[1]);
                String name = parts[2];
                TaskStatus status = TaskStatus.valueOf(parts[3]);
                String description = parts[4];

                switch (type) {
                    case TASK -> super.addNewTask(new Task(id, name, description, status));
                    case EPIC -> {
                        Epic epic = new Epic(id, name, description);
                        epic.setStatus(status);
                        super.addNewEpic(epic);
                    }
                    case SUBTASK -> {
                        int epicId = Integer.parseInt(parts[5]);
                        Subtask subtask = new Subtask(id, name, description, status, epicId);
                        super.addNewSubtask(subtask);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void save() {
        ArrayList<Task> allTasks = new ArrayList<>(getTasks());
        allTasks.addAll(getEpics());
        allTasks.addAll(getSubtasks());
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("id,type,name,status,description,epicId\n");
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

        Task task1 = new Task("Задача 1", "Описание задачи 1", TaskStatus.NEW);
        manager1.addNewTask(task1);
        Epic epic1 = new Epic("Эпик с подзадачами", "Эпик с тремя подзадачами");
        int epic1Id = manager1.addNewEpic(epic1);
        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи 1", TaskStatus.NEW, epic1Id);

        manager1.addNewSubtask(subtask1);

        System.out.println("Первый менеджер содержит:");
        printAllTasks(manager1);

        FileBackedTaskManager manager2 = new FileBackedTaskManager(testFile);

        System.out.println("Второй менеджер содержит:");
        printAllTasks(manager2);

        if (manager1.getTasks().size() == manager2.getTasks().size() &&
                manager1.getEpics().size() == manager2.getEpics().size() &&
                manager1.getSubtasks().size() == manager2.getSubtasks().size()) {
            System.out.println("✓ Проверка пройдена! Все задачи успешно восстановлены.");
        } else {
            System.out.println("✗ Проверка не пройдена! Данные не совпадают.");
        }
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("Все задачи:");
        System.out.println("- Простые задачи (" + manager.getTasks().size() + "):");
        for (Task task : manager.getTasks()) {
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