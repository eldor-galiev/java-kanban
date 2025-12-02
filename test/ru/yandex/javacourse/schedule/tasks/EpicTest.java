package test.ru.yandex.javacourse.schedule.tasks;

import org.junit.jupiter.api.Test;
import main.ru.yandex.javacourse.schedule.tasks.Epic;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EpicTest {

    @Test
    public void testEqualityById() {
        Epic e0 = new Epic(1, "Test 1", "Testing task 1");
        Epic e1 = new Epic(1, "Test 2", "Testing task 2");
        assertEquals(e0, e1, "task and subentities should be compared by id");
    }
}
