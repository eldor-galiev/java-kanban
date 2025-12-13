package test.ru.yandex.javacourse.schedule.api;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import main.ru.yandex.javacourse.schedule.api.HttpTaskServer;
import main.ru.yandex.javacourse.schedule.api.handler.BaseHttpHandler;
import main.ru.yandex.javacourse.schedule.manager.TaskManager;
import main.ru.yandex.javacourse.schedule.tasks.Epic;
import main.ru.yandex.javacourse.schedule.tasks.Subtask;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class HttpServerSubtasksTest {
    private TaskManager manager;
    private HttpTaskServer taskServer;
    private Gson gson;
    private HttpClient client;
    private Epic epic;

    @BeforeEach
    public void setUp() throws IOException {
        taskServer = new HttpTaskServer();
        manager = taskServer.getTaskManager();
        taskServer.start();
        gson = BaseHttpHandler.getGson();
        client = HttpClient.newHttpClient();

        epic = new Epic("Test Epic", "Epic Description");
        epic.setId(manager.addNewEpic(epic));
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void testAddSubtask() throws IOException, InterruptedException {
        Subtask subtask = new Subtask("Test Subtask", "Subtask Description",
                TaskStatus.NEW, epic.getId(), LocalDateTime.now(), Duration.ofMinutes(15));
        String subtaskJson = gson.toJson(subtask);

        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Subtask> subtasks = manager.getSubtasks();
        assertEquals(1, subtasks.size());
        assertEquals("Test Subtask", subtasks.getFirst().getName());
        assertEquals(epic.getId(), subtasks.getFirst().getEpicId());
    }

    @Test
    public void testGetSubtaskById() throws IOException, InterruptedException {
        Subtask subtask = new Subtask("Subtask", "Description",
                TaskStatus.NEW, epic.getId(), LocalDateTime.now(), Duration.ofMinutes(15));
        manager.addNewSubtask(subtask);
        int subtaskId = subtask.getId();

        URI url = URI.create("http://localhost:8080/subtasks/" + subtaskId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Subtask retrievedSubtask = gson.fromJson(response.body(), Subtask.class);
        assertEquals(subtaskId, retrievedSubtask.getId());
    }

    @Test
    public void testGetAllSubtasks() throws IOException, InterruptedException {
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1",
                TaskStatus.NEW, epic.getId(), LocalDateTime.now(), Duration.ofMinutes(10));
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2",
                TaskStatus.IN_PROGRESS, epic.getId(), LocalDateTime.now().plusHours(1), Duration.ofMinutes(20));

        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);

        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Subtask> subtasks = gson.fromJson(response.body(), new TypeToken<List<Subtask>>(){}.getType());
        assertEquals(2, subtasks.size());
    }

    @Test
    public void testDeleteSubtask() throws IOException, InterruptedException {
        Subtask subtask = new Subtask("To Delete", "Description",
                TaskStatus.NEW, epic.getId(), LocalDateTime.now(), Duration.ofMinutes(15));
        manager.addNewSubtask(subtask);
        int subtaskId = subtask.getId();

        URI url = URI.create("http://localhost:8080/subtasks/" + subtaskId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        assertNull(manager.getSubtask(subtaskId));
        assertEquals(0, manager.getSubtasks().size());
    }
}