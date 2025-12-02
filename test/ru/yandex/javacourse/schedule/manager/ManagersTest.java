package test.ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.Test;
import main.ru.yandex.javacourse.schedule.manager.Managers;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ManagersTest {

    @Test
    public void testDefaultManagersNotNull() {
        assertNotNull(Managers.getDefault(), "default manager should not be null");
        assertNotNull(Managers.getDefaultHistory(), "default history managers should not be null");
    }

}
