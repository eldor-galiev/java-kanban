package test.ru.yandex.javacourse.schedule.manager;

import main.ru.yandex.javacourse.schedule.manager.HistoryManager;
import main.ru.yandex.javacourse.schedule.manager.Managers;

public class InMemoryHistoryManagerTest extends HistoryManagerTest {

    @Override
    protected HistoryManager createHistoryManager() {
        return Managers.getDefaultHistory();
    }
}