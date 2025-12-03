package main.ru.yandex.javacourse.schedule.exception;

public class ManagerSaveException extends RuntimeException {
    public ManagerSaveException(final String message) {
        super(message);
    }
}