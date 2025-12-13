package main.ru.yandex.javacourse.schedule.api.handler;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import main.ru.yandex.javacourse.schedule.exception.TaskOverlapException;
import main.ru.yandex.javacourse.schedule.manager.TaskManager;
import main.ru.yandex.javacourse.schedule.tasks.Task;

import java.io.IOException;
import java.util.List;

public class TaskHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public TaskHandler(TaskManager taskManager) {
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
            Task task = taskManager.getTask(id);
            if (task != null) {
                sendSuccess(exchange, task);
            } else {
                sendNotFound(exchange, "Задача с id=" + id + " не найдена");
            }
        } else {
            List<Task> tasks = taskManager.getTasks();
            sendSuccess(exchange, tasks);
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        try {
            String body = readRequestBody(exchange);
            Task task = gson.fromJson(body, Task.class);

            if (task.getId() == 0) {
                taskManager.addNewTask(task);
            } else {
                taskManager.updateTask(task);
            }
            sendCreated(exchange);
        } catch (JsonSyntaxException e) {
            sendError(exchange, "Неверный формат JSON");
        } catch (TaskOverlapException e) {
            sendHasInteractions(exchange, e.getMessage());
        }
    }

    private void handleDelete(HttpExchange exchange, int id) throws IOException {
        if (id != -1) {
            taskManager.deleteTask(id);
            sendSuccess(exchange, "Задача удалена");
        } else {
            sendError(exchange, "Необходимо отправить id задачи");
        }
    }
}