package main.ru.yandex.javacourse.schedule.manager;

import java.io.File;
import java.io.IOException;

/**
 * Default managers.
 *
 * @author Vladimir Ivanov (ivanov.vladimir.l@gmail.com)
 */
public class Managers {
	public static TaskManager getDefault() {
        try {
            return loadFromFile(File.createTempFile("all_tasks", ".csv"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

	public static HistoryManager getDefaultHistory() {
		return new InMemoryHistoryManager();
	}

    public static FileBackedTaskManager loadFromFile(File file) {
        return new FileBackedTaskManager(file.getPath());
    }
}
