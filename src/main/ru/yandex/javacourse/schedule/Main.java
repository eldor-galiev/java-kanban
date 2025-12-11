package main.ru.yandex.javacourse.schedule;

import main.ru.yandex.javacourse.schedule.manager.Managers;
import main.ru.yandex.javacourse.schedule.manager.TaskManager;
import main.ru.yandex.javacourse.schedule.tasks.Epic;
import main.ru.yandex.javacourse.schedule.tasks.Subtask;
import main.ru.yandex.javacourse.schedule.tasks.Task;
import main.ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        System.out.println("=== ПОЛЬЗОВАТЕЛЬСКИЙ СЦЕНАРИЙ ===");
        System.out.println();

        System.out.println("1. СОЗДАНИЕ ЗАДАЧ:");

        Task task1 = new Task("Задача 1", "Описание задачи 1", TaskStatus.NEW,
                LocalDateTime.now(), Duration.ofMinutes(15));
        Task task2 = new Task("Задача 2", "Описание задачи 2", TaskStatus.NEW,
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(10));

        Epic epicWithSubtasks = new Epic("Эпик с подзадачами", "Эпик с тремя подзадачами");
        Epic epicWithoutSubtasks = new Epic("Пустой эпик", "Эпик без подзадач");

        int taskId1 = manager.addNewTask(task1);
        int taskId2 = manager.addNewTask(task2);
        int epicWithSubtasksId = manager.addNewEpic(epicWithSubtasks);
        int epicWithoutSubtasksId = manager.addNewEpic(epicWithoutSubtasks);

        System.out.println("Создано:");
        System.out.println("- Задача 1 (id: " + taskId1 + ")");
        System.out.println("- Задача 2 (id: " + taskId2 + ")");
        System.out.println("- Эпик с подзадачами (id: " + epicWithSubtasksId + ")");
        System.out.println("- Пустой эпик (id: " + epicWithoutSubtasksId + ")");

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи 1", TaskStatus.NEW, epicWithSubtasksId,
                LocalDateTime.now(), Duration.ofMinutes(10));
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание подзадачи 2", TaskStatus.NEW, epicWithSubtasksId,
                LocalDateTime.now().plusHours(2), Duration.ofMinutes(30));
        Subtask subtask3 = new Subtask("Подзадача 3", "Описание подзадачи 3", TaskStatus.NEW, epicWithSubtasksId,
                LocalDateTime.now().plusHours(3), Duration.ofMinutes(60));

        Integer subtaskId1 = manager.addNewSubtask(subtask1);
        Integer subtaskId2 = manager.addNewSubtask(subtask2);
        Integer subtaskId3 = manager.addNewSubtask(subtask3);

        System.out.println("- Подзадача 1 (id: " + subtaskId1 + ", эпик: " + epicWithSubtasksId + ")");
        System.out.println("- Подзадача 2 (id: " + subtaskId2 + ", эпик: " + epicWithSubtasksId + ")");
        System.out.println("- Подзадача 3 (id: " + subtaskId3 + ", эпик: " + epicWithSubtasksId + ")");

        System.out.println();

        System.out.println("2. ЗАПРОСЫ В РАЗНОМ ПОРЯДКЕ (РАУНД 1):");
        System.out.println("Запросы: Задача 2 -> Эпик с подзадачами -> Подзадача 1");

        manager.getTask(taskId2);
        manager.getEpic(epicWithSubtasksId);
        manager.getSubtask(subtaskId1);

        printHistory(manager);
        checkForDuplicates(manager);

        System.out.println("3. ЗАПРОСЫ В РАЗНОМ ПОРЯДКЕ (РАУНД 2):");
        System.out.println("Запросы: Подзадача 3 -> Задача 1 -> Пустой эпик -> Задача 2 (повторно)");

        manager.getSubtask(subtaskId3);
        manager.getTask(taskId1);
        manager.getEpic(epicWithoutSubtasksId);
        manager.getTask(taskId2); // Повторный запрос

        printHistory(manager);
        checkForDuplicates(manager);

        System.out.println("4. ЗАПРОСЫ В РАЗНОМ ПОРЯДКЕ (РАУНД 3):");
        System.out.println("Запросы: Подзадача 2 -> Эпик с подзадачами (повторно) -> Задача 1 (повторно)");

        manager.getSubtask(subtaskId2);
        manager.getEpic(epicWithSubtasksId); // Повторный запрос
        manager.getTask(taskId1); // Повторный запрос

        printHistory(manager);
        checkForDuplicates(manager);

        System.out.println("5. УДАЛЕНИЕ ЗАДАЧИ 2 (КОТОРАЯ ЕСТЬ В ИСТОРИИ):");
        System.out.println("Удаляем задачу с id: " + taskId2);

        manager.deleteTask(taskId2);

        printHistory(manager);
        checkForDuplicates(manager);

        System.out.println("Проверка: Задача 2 должна отсутствовать в истории.");
        boolean task2InHistory = isTaskInHistory(manager, taskId2);
        System.out.println("Задача 2 в истории: " + (task2InHistory ? "ДА (ОШИБКА!)" : "НЕТ (корректно)"));

        System.out.println();

        System.out.println("6. УДАЛЕНИЕ ЭПИКА С ПОДЗАДАЧАМИ:");
        System.out.println("Удаляем эпик с id: " + epicWithSubtasksId);
        System.out.println("При этом должны удалиться:");
        System.out.println("- Сам эпик");
        System.out.println("- Подзадача 1 (id: " + subtaskId1 + ")");
        System.out.println("- Подзадача 2 (id: " + subtaskId2 + ")");
        System.out.println("- Подзадача 3 (id: " + subtaskId3 + ")");

        manager.deleteEpic(epicWithSubtasksId);

        printHistory(manager);
        checkForDuplicates(manager);

        System.out.println("Проверка удаления из истории:");
        boolean epicInHistory = isTaskInHistory(manager, epicWithSubtasksId);
        boolean subtask1InHistory = isTaskInHistory(manager, subtaskId1);
        boolean subtask2InHistory = isTaskInHistory(manager, subtaskId2);
        boolean subtask3InHistory = isTaskInHistory(manager, subtaskId3);

        System.out.println("Эпик в истории: " + (epicInHistory ? "ДА (ОШИБКА!)" : "НЕТ (корректно)"));
        System.out.println("Подзадача 1 в истории: " + (subtask1InHistory ? "ДА (ОШИБКА!)" : "НЕТ (корректно)"));
        System.out.println("Подзадача 2 в истории: " + (subtask2InHistory ? "ДА (ОШИБКА!)" : "НЕТ (корректно)"));
        System.out.println("Подзадача 3 в истории: " + (subtask3InHistory ? "ДА (ОШИБКА!)" : "НЕТ (корректно)"));

        System.out.println();

        System.out.println("7. ФИНАЛЬНОЕ СОСТОЯНИЕ МЕНЕДЖЕРА:");
        printAllTasks(manager);
    }

    private static void printHistory(TaskManager manager) {
        List<Task> history = manager.getHistory();
        System.out.println("История (" + history.size() + " элементов):");
        if (history.isEmpty()) {
            System.out.println("(пусто)");
        } else {
            for (Task task : history) {
                String type = getTaskType(task);
                System.out.println("- " + type + " [id: " + task.getId() +
                        ", название: \"" + task.getName() + "\"]");
            }
        }
        System.out.println();
    }

    private static void checkForDuplicates(TaskManager manager) {
        List<Task> history = manager.getHistory();
        Set<Integer> ids = new HashSet<>();
        Set<Integer> duplicateIds = new HashSet<>();

        for (Task task : history) {
            if (!ids.add(task.getId())) {
                duplicateIds.add(task.getId());
            }
        }

        if (duplicateIds.isEmpty()) {
            System.out.println("✓ В истории нет дубликатов");
        } else {
            System.out.println("✗ ОШИБКА: В истории найдены дубликаты задач с id: " + duplicateIds);
        }
        System.out.println();
    }

    private static boolean isTaskInHistory(TaskManager manager, int id) {
        return manager.getHistory().stream()
                .anyMatch(task -> task.getId() == id);
    }

    private static String getTaskType(Task task) {
        if (task instanceof Epic) {
            return "Эпик";
        } else if (task instanceof Subtask) {
            return "Подзадача";
        } else {
            return "Задача";
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