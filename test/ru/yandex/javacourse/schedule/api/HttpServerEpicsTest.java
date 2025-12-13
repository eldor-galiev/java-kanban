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

import static org.junit.jupiter.api.Assertions.*;

public class HttpServerEpicsTest {
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
    public void testAddEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Epic Description");
        String epicJson = gson.toJson(epic);

        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Статус код должен быть 201 (Created)");

        List<Epic> epicsFromManager = manager.getEpics();

        assertNotNull(epicsFromManager, "Эпики не возвращаются");
        assertEquals(1, epicsFromManager.size(), "Некорректное количество эпиков");
        assertEquals("Test Epic", epicsFromManager.getFirst().getName(), "Некорректное имя эпика");
        assertEquals(TaskStatus.NEW, epicsFromManager.getFirst().getStatus(), "Статус нового эпика должен быть NEW");
    }

    @Test
    public void testGetEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Epic Description");
        manager.addNewEpic(epic);
        int epicId = epic.getId();

        URI url = URI.create("http://localhost:8080/epics/" + epicId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Статус код должен быть 200");

        Epic retrievedEpic = gson.fromJson(response.body(), Epic.class);
        assertNotNull(retrievedEpic, "Эпик не должен быть null");
        assertEquals(epicId, retrievedEpic.getId(), "ID эпика должен совпадать");
        assertEquals("Test Epic", retrievedEpic.getName(), "Имя эпика должно совпадать");
    }

    @Test
    public void testGetEpicByIdNotFound() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/epics/999");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Статус код должен быть 404 для несуществующего эпика");
    }

    @Test
    public void testGetAllEpics() throws IOException, InterruptedException {
        Epic epic1 = new Epic("Epic 1", "Description 1");
        Epic epic2 = new Epic("Epic 2", "Description 2");

        manager.addNewEpic(epic1);
        manager.addNewEpic(epic2);

        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Epic> epics = gson.fromJson(response.body(), new TypeToken<List<Epic>>(){}.getType());
        assertEquals(2, epics.size(), "Должно быть 2 эпика");
    }

    @Test
    public void testUpdateEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Original Epic", "Original Description");
        manager.addNewEpic(epic);

        epic.setName("Updated Epic");
        epic.setDescription("Updated Description");

        String epicJson = gson.toJson(epic);
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        Epic updatedEpic = manager.getEpic(epic.getId());
        assertEquals("Updated Epic", updatedEpic.getName());
        assertEquals("Updated Description", updatedEpic.getDescription());
    }

    @Test
    public void testDeleteEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("To Delete Epic", "Description");
        manager.addNewEpic(epic);
        int epicId = epic.getId();

        URI url = URI.create("http://localhost:8080/epics/" + epicId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        assertNull(manager.getEpic(epicId), "Эпик должен быть удален");
        assertEquals(0, manager.getEpics().size(), "Список эпиков должен быть пустым");
    }

    @Test
    public void testDeleteEpicWithSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic with Subtasks", "Description");
        manager.addNewEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description 1",
                TaskStatus.NEW, epic.getId(), LocalDateTime.now(), Duration.ofMinutes(10));
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2",
                TaskStatus.IN_PROGRESS, epic.getId(), LocalDateTime.now().plusHours(1), Duration.ofMinutes(10));

        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);

        int epicId = epic.getId();

        URI url = URI.create("http://localhost:8080/epics/" + epicId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        assertNull(manager.getEpic(epicId), "Эпик должен быть удален");
        assertEquals(0, manager.getSubtasks().size(), "Подзадачи эпика должны быть удалены");
    }

    @Test
    public void testGetEpicSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Parent Epic", "Description");
        manager.addNewEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description 1",
                TaskStatus.NEW, epic.getId(), LocalDateTime.now(), Duration.ofMinutes(10));
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2",
                TaskStatus.IN_PROGRESS, epic.getId(), LocalDateTime.now().plusHours(1), Duration.ofMinutes(10));

        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);

        int epicId = epic.getId();

        URI url = URI.create("http://localhost:8080/epics/" + epicId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Epic retrievedEpic = gson.fromJson(response.body(), Epic.class);
        assertNotNull(retrievedEpic, "Эпик не должен быть null");

        List<Integer> subtaskIds = retrievedEpic.getSubtaskIds();
        assertEquals(2, subtaskIds.size(), "У эпика должно быть 2 подзадачи");
        assertTrue(subtaskIds.contains(subtask1.getId()), "Должна содержаться подзадача 1");
        assertTrue(subtaskIds.contains(subtask2.getId()), "Должна содержаться подзадача 2");
    }

    @Test
    public void testUpdateEpicStatusBasedOnSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Status Epic", "Description");
        manager.addNewEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description 1",
                TaskStatus.NEW, epic.getId(), LocalDateTime.now(), Duration.ofMinutes(10));
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2",
                TaskStatus.NEW, epic.getId(), LocalDateTime.now().plusHours(1), Duration.ofMinutes(10));

        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);

        assertEquals(TaskStatus.NEW, epic.getStatus(), "Статус должен быть NEW");

        subtask1.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask1);

        URI url = URI.create("http://localhost:8080/epics/" + epic.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Epic updatedEpic = gson.fromJson(response.body(), Epic.class);

        assertEquals(TaskStatus.IN_PROGRESS, updatedEpic.getStatus(),
                "Статус должен быть IN_PROGRESS при смешанных статусах подзадач");
    }

    @Test
    public void testInvalidEpicJson() throws IOException, InterruptedException {
        String invalidJson = "{invalid json for epic}";

        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(invalidJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(500, response.statusCode(), "Статус код должен быть 500 для невалидного JSON");
    }

    @Test
    public void testMethodNotAllowed() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .PUT(HttpRequest.BodyPublishers.ofString("{}"))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(500, response.statusCode(), "Метод PUT не должен поддерживаться");
    }
}