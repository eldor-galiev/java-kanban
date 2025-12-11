package test.ru.yandex.javacourse.schedule.manager;

import main.ru.yandex.javacourse.schedule.manager.InMemoryTaskManager;
import main.ru.yandex.javacourse.schedule.manager.Managers;
import main.ru.yandex.javacourse.schedule.manager.TaskManager;

public class InMemoryTaskManagerTest extends TaskManagerTest<TaskManager> {

    @Override
    protected TaskManager createManager() {
        return Managers.getDefault();
    }
}