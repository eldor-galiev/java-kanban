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

public class HttpServerPrioritizedTest {
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
    public void testGetPrioritizedTasks() throws IOException, InterruptedException {
        LocalDateTime now = LocalDateTime.now();

        Task task2 = new Task("Task 2", "Later task",
                TaskStatus.NEW, now.plusHours(2), Duration.ofMinutes(10));
        Task task1 = new Task("Task 1", "Earlier task",
                TaskStatus.NEW, now.plusHours(1), Duration.ofMinutes(5));

        manager.addNewTask(task2);
        manager.addNewTask(task1);

        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> prioritized = gson.fromJson(response.body(), new TypeToken<List<Task>>(){}.getType());
        assertEquals(2, prioritized.size());
        assertEquals(task1.getId(), prioritized.get(0).getId(), "Первая задача должна быть task1 (раньше по времени)");
        assertEquals(task2.getId(), prioritized.get(1).getId(), "Вторая задача должна быть task2 (позже по времени)");
    }

    @Test
    public void testGetPrioritizedTasksWithoutTime() throws IOException, InterruptedException {
        Task task1 = new Task("Task 1", "No time", TaskStatus.NEW);
        Task task2 = new Task("Task 2", "No time 2", TaskStatus.NEW);

        manager.addNewTask(task1);
        manager.addNewTask(task2);

        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> prioritized = gson.fromJson(response.body(), new TypeToken<List<Task>>(){}.getType());
        assertEquals(0, prioritized.size(), "Задачи без времени не должны возвращаться");
    }
}