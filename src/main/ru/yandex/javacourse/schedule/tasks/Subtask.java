package main.ru.yandex.javacourse.schedule.tasks;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
	protected int epicId;

	public Subtask(int id, String name, String description, TaskStatus status, int epicId,
                   LocalDateTime startTime, Duration duration) {
		super(id, name, description, status, startTime, duration);
		this.epicId = epicId;
	}

    public Subtask(int id, String name, String description, TaskStatus status, int epicId) {
        super(id, name, description, status);
        this.epicId = epicId;
    }

	public Subtask(String name, String description, TaskStatus status, int epicId,
                   LocalDateTime startTime, Duration duration) {
		super(name, description, status, startTime, duration);
		this.epicId = epicId;
	}

    public Subtask(String name, String description, TaskStatus status, int epicId) {
        super(name, description, status);
        this.epicId = epicId;
    }

	public int getEpicId() {
		return epicId;
	}

    @Override
    public String toString() {
        return id + "," + getType() + "," + name + "," + status + "," + description
                + "," + (startTime != null ? startTime : "")
                + "," + (duration != null ? duration : "") + "," + epicId;
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }
}
