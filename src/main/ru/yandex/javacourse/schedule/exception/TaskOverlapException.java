package main.ru.yandex.javacourse.schedule.exception;

public class TaskOverlapException extends RuntimeException {
    public TaskOverlapException(final String message) {
        super(message);
    }
}