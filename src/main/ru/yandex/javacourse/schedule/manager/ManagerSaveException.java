package main.ru.yandex.javacourse.schedule.manager;

public class ManagerSaveException extends RuntimeException {
    public ManagerSaveException(final String message) {
        super(message);
    }
}