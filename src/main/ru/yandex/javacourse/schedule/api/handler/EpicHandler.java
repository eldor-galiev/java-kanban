package main.ru.yandex.javacourse.schedule.api.handler;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import main.ru.yandex.javacourse.schedule.manager.TaskManager;
import main.ru.yandex.javacourse.schedule.tasks.Epic;

import java.io.IOException;
import java.util.List;

public class EpicHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public EpicHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            int id = parseIdFromPath(exchange.getRequestURI().getPath());

            switch (method) {
                case "GET":
                    handleGet(exchange, id);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "DELETE":
                    handleDelete(exchange, id);
                    break;
                default:
                    sendError(exchange, "Метод не поддерживается");
            }
        } catch (Exception e) {
            sendError(exchange, "Внутренняя ошибка сервера: " + e.getMessage());
        }
    }

    private void handleGet(HttpExchange exchange, int id) throws IOException {
        if (id != -1) {
            Epic epic = taskManager.getEpic(id);
            if (epic != null) {
                sendSuccess(exchange, epic);
            } else {
                sendNotFound(exchange, "Эпик с id=" + id + " не найден");
            }
        } else {
            List<Epic> epics = taskManager.getEpics();
            sendSuccess(exchange, epics);
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        try {
            String body = readRequestBody(exchange);
            Epic epic = gson.fromJson(body, Epic.class);

            if (epic.getId() == 0) {
                taskManager.addNewEpic(epic);
            } else {
                taskManager.updateEpic(epic);
            }
            sendCreated(exchange);
        } catch (JsonSyntaxException e) {
            sendError(exchange, "Неверный формат JSON");
        }
    }

    private void handleDelete(HttpExchange exchange, int id) throws IOException {
        if (id != -1) {
            taskManager.deleteEpic(id);
            sendSuccess(exchange, "Эпик удален");
        } else {
            sendError(exchange, "Необходимо отправить id подзадачи");
        }
    }
}