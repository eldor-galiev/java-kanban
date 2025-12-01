package main.ru.yandex.javacourse.schedule.manager;

import main.ru.yandex.javacourse.schedule.tasks.Task;

public class Node {
    Task item;
    Node next;
    Node prev;

    Node(Node prev, Task element, Node next) {
        this.item = element;
        this.next = next;
        this.prev = prev;
    }

}