package main.ru.yandex.javacourse.schedule.manager;

import java.util.List;

import main.ru.yandex.javacourse.schedule.tasks.Task;

public interface HistoryManager {

	List<Task> getHistory();

	void addTask(Task task);

    void remove(int id);

}
