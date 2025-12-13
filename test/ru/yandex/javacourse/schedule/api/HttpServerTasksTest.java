package test.ru.yandex.javacourse.schedule.api;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import main.ru.yandex.javacourse.schedule.api.HttpTaskServer;
import main.ru.yandex.javacourse.schedule.api.handler.BaseHttpHandler;
import main.ru.yandex.javacourse.schedule.manager.TaskManager;
import main.ru.yandex.javacourse.schedule.tasks.Task;
import main.ru.yandex.javacourse.schedule.tasks.TaskStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpServerTasksTest {
    private TaskManager manager;
    private HttpTaskServer taskServer;
    private Gson gson;
    private HttpClient client;

    @BeforeEach
    public void setUp() throws IOException {
        taskServer = new HttpTaskServer();
        manager = taskServer.getTaskManager();
        taskServer.start();
        gson = BaseHttpHandler.getGson();
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void testAddTask() throws IOException, InterruptedException {
        Task task = new Task("Test 2", "Testing task 2",
                TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(5));
        String taskJson = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Статус код должен быть 201 (Created)");

        List<Task> tasksFromManager = manager.getTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test 2", tasksFromManager.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    public void testGetTaskById() throws IOException, InterruptedException {
        Task task = new Task("Test Task", "Description", TaskStatus.NEW,
                LocalDateTime.now(), Duration.ofMinutes(10));
        manager.addNewTask(task);
        int taskId = task.getId();

        URI url = URI.create("http://localhost:8080/tasks/" + taskId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Статус код должен быть 200");

        Task retrievedTask = gson.fromJson(response.body(), Task.class);
        assertNotNull(retrievedTask, "Задача не должна быть null");
        assertEquals(taskId, retrievedTask.getId(), "ID задачи должен совпадать");
    }

    @Test
    public void testGetTaskByIdNotFound() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/999");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Статус код должен быть 404 для несуществующей задачи");
    }

    @Test
    public void testGetAllTasks() throws IOException, InterruptedException {
        Task task1 = new Task("Task 1", "Description 1",
                TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(5));
        Task task2 = new Task("Task 2", "Description 2",
                TaskStatus.IN_PROGRESS, LocalDateTime.now().plusHours(1), Duration.ofMinutes(10));

        manager.addNewTask(task1);
        manager.addNewTask(task2);

        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> tasks = gson.fromJson(response.body(), new TypeToken<List<Task>>(){}.getType());
        assertEquals(2, tasks.size(), "Должно быть 2 задачи");
    }

    @Test
    public void testUpdateTask() throws IOException, InterruptedException {
        Task task = new Task("Original", "Original Description",
                TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(5));
        manager.addNewTask(task);

        task.setName("Updated");
        task.setDescription("Updated Description");
        task.setStatus(TaskStatus.DONE);

        String taskJson = gson.toJson(task);
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        Task updatedTask = manager.getTask(task.getId());
        assertEquals("Updated", updatedTask.getName());
        assertEquals(TaskStatus.DONE, updatedTask.getStatus());
    }

    @Test
    public void testDeleteTask() throws IOException, InterruptedException {
        Task task = new Task("To Delete", "Description",
                TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(5));
        manager.addNewTask(task);
        int taskId = task.getId();

        URI url = URI.create("http://localhost:8080/tasks/" + taskId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        assertNull(manager.getTask(taskId), "Задача должна быть удалена");
    }

    @Test
    public void testInvalidJson() throws IOException, InterruptedException {
        String invalidJson = "{invalid json}";

        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(invalidJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(500, response.statusCode(), "Статус код должен быть 500 для невалидного JSON");
    }

    @Test
    public void testTaskOverlap() throws IOException, InterruptedException {
        LocalDateTime startTime = LocalDateTime.now();
        Task task1 = new Task("Task 1", "Description",
                TaskStatus.NEW, startTime, Duration.ofMinutes(30));
        manager.addNewTask(task1);

        Task task2 = new Task("Task 2", "Overlapping",
                TaskStatus.NEW, startTime.plusMinutes(15), Duration.ofMinutes(30));
        String taskJson = gson.toJson(task2);

        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode(), "Статус код должен быть 406 при пересечении задач");
    }
}