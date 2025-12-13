package main.ru.yandex.javacourse.schedule.api.handler;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import main.ru.yandex.javacourse.schedule.manager.TaskManager;
import main.ru.yandex.javacourse.schedule.tasks.Subtask;

import java.io.IOException;
import java.util.List;

public class SubtaskHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public SubtaskHandler(TaskManager taskManager) {
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
            Subtask subtask = taskManager.getSubtask(id);
            if (subtask != null) {
                sendSuccess(exchange, subtask);
            } else {
                sendNotFound(exchange, "Подзадача с id=" + id + " не найдена");
            }
        } else {
            List<Subtask> subtasks = taskManager.getSubtasks();
            sendSuccess(exchange, subtasks);
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        try {
            String body = readRequestBody(exchange);
            Subtask subtask = gson.fromJson(body, Subtask.class);

            if (subtask.getId() == 0) {
                taskManager.addNewSubtask(subtask);
            } else {
                taskManager.updateSubtask(subtask);
            }
            sendCreated(exchange);
        } catch (JsonSyntaxException e) {
            sendError(exchange, "Неверный формат JSON");
        } catch (IllegalArgumentException e) {
            sendHasInteractions(exchange, e.getMessage());
        }
    }

    private void handleDelete(HttpExchange exchange, int id) throws IOException {
        if (id != -1) {
            taskManager.deleteSubtask(id);
            sendSuccess(exchange, "Подзадача удалена");
        } else {
            sendError(exchange, "Необходимо отправить id подзадачи");
        }
    }
}