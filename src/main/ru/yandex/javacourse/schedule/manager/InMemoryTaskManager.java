package main.ru.yandex.javacourse.schedule.manager;

import static main.ru.yandex.javacourse.schedule.tasks.TaskStatus.IN_PROGRESS;
import static main.ru.yandex.javacourse.schedule.tasks.TaskStatus.NEW;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import main.ru.yandex.javacourse.schedule.tasks.Epic;
import main.ru.yandex.javacourse.schedule.tasks.Subtask;
import main.ru.yandex.javacourse.schedule.tasks.Task;
import main.ru.yandex.javacourse.schedule.tasks.TaskStatus;

public class InMemoryTaskManager implements TaskManager {

	protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final TreeSet<Task> prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime));
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
	protected int generatorId = 0;
	private final HistoryManager historyManager = Managers.getDefaultHistory();

	@Override
	public ArrayList<Task> getTasks() {
		return new ArrayList<>(this.tasks.values());
	}

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

	@Override
	public ArrayList<Subtask> getSubtasks() {
		return new ArrayList<>(subtasks.values());
	}

	@Override
	public ArrayList<Epic> getEpics() {
		return new ArrayList<>(epics.values());
	}

	@Override
	public ArrayList<Subtask> getEpicSubtasks(int epicId) {
		ArrayList<Subtask> tasks = new ArrayList<>();
		Epic epic = epics.get(epicId);
		if (epic == null) {
			return null;
		}
		epic.getSubtaskIds().forEach(id -> {
            tasks.add(subtasks.get(id));
        });
		return tasks;
	}

	@Override
	public Task getTask(int id) {
		final Task task = tasks.get(id);
		historyManager.addTask(task);
		return task;
	}

	@Override
	public Subtask getSubtask(int id) {
		final Subtask subtask = subtasks.get(id);
		historyManager.addTask(subtask);
		return subtask;
	}

	@Override
	public Epic getEpic(int id) {
		final Epic epic = epics.get(id);
		historyManager.addTask(epic);
		return epic;
	}

	@Override
	public int addNewTask(Task task) {
		final int id = ++generatorId;
		task.setId(id);
		tasks.put(id, task);
        if (task.getStartTime() != null && !hasOverlapWithAnyTask(task)) {
            prioritizedTasks.add(task);
        }
		return id;
	}

	@Override
	public int addNewEpic(Epic epic) {
		final int id = ++generatorId;
		epic.setId(id);
		epics.put(id, epic);
		return id;

	}

	@Override
	public Integer addNewSubtask(Subtask subtask) {
		final int epicId = subtask.getEpicId();
		Epic epic = epics.get(epicId);
		if (epic == null) {
			return null;
		}
		final int id = ++generatorId;
		subtask.setId(id);
		subtasks.put(id, subtask);
		epic.addSubtaskId(subtask.getId());
        if (subtask.getStartTime() != null && !hasOverlapWithAnyTask(subtask)) {
            prioritizedTasks.add(subtask);
        }
		updateEpicStatus(epicId);
        updateEpicTimings(epicId);
		return id;
	}

	@Override
	public void updateTask(Task task) {
		final int id = task.getId();
		final Task savedTask = tasks.get(id);
		if (savedTask == null) {
			return;
		}
		tasks.put(id, task);
        if (task.getStartTime() != null && !hasOverlapWithAnyTask(task)) {
            prioritizedTasks.add(task);
        }
	}

	@Override
	public void updateEpic(Epic epic) {
		final Epic savedEpic = epics.get(epic.getId());
		savedEpic.setName(epic.getName());
		savedEpic.setDescription(epic.getDescription());
	}

	@Override
	public void updateSubtask(Subtask subtask) {
		final int id = subtask.getId();
		final int epicId = subtask.getEpicId();
		final Subtask savedSubtask = subtasks.get(id);
		if (savedSubtask == null) {
			return;
		}
		final Epic epic = epics.get(epicId);
		if (epic == null) {
			return;
		}
		subtasks.put(id, subtask);
        if (subtask.getStartTime() != null && !hasOverlapWithAnyTask(subtask)) {
            prioritizedTasks.add(subtask);
        }
		updateEpicStatus(epicId);
        updateEpicTimings(epicId);
	}

	@Override
	public void deleteTask(int id) {
        Task removingTask = tasks.remove(id);
        prioritizedTasks.remove(removingTask);
        historyManager.remove(id);
	}

	@Override
	public void deleteEpic(int id) {
		final Epic epic = epics.remove(id);
        historyManager.remove(id);
        epic.getSubtaskIds().forEach(subtaskId -> {
			subtasks.remove(subtaskId);
            historyManager.remove(subtaskId);
        });
	}

	@Override
	public void deleteSubtask(int id) {
		Subtask subtask = subtasks.remove(id);
        prioritizedTasks.remove(subtask);
        historyManager.remove(id);
		if (subtask == null) {
			return;
		}
		Epic epic = epics.get(subtask.getEpicId());
		epic.removeSubtask(id);
		updateEpicStatus(epic.getId());
        updateEpicTimings(epic.getId());
	}

	@Override
	public void deleteTasks() {
        tasks.forEach((key, value) -> {
            historyManager.remove(key);
            prioritizedTasks.remove(value);
        });
		tasks.clear();
	}

	@Override
	public void deleteSubtasks() {
        subtasks.forEach((key, value) -> {
            historyManager.remove(key);
            prioritizedTasks.remove(value);
        });
		epics.values().forEach((epic) -> {
			epic.cleanSubtaskIds();
			updateEpicStatus(epic.getId());
            updateEpicTimings(epic.getId());
		});
		subtasks.clear();
	}

	@Override
	public void deleteEpics() {
        epics.forEach((key, value) -> {
            historyManager.remove(key);
            value.getSubtaskIds().forEach(historyManager::remove);
        });

		epics.clear();
		subtasks.clear();
	}

	@Override
	public List<Task> getHistory() {
		return historyManager.getHistory();
	}

	public void updateEpicStatus(int epicId) {
		Epic epic = epics.get(epicId);
		List<Integer> subs = epic.getSubtaskIds();
		if (subs.isEmpty()) {
			epic.setStatus(NEW);
			return;
		}
		TaskStatus status = null;
		for (int id : subs) {
			final Subtask subtask = subtasks.get(id);
			if (status == null) {
				status = subtask.getStatus();
				continue;
			}

			if (status == subtask.getStatus()
					&& status != IN_PROGRESS) {
				continue;
			}
			epic.setStatus(IN_PROGRESS);
			return;
		}
		epic.setStatus(status);
	}

    public void updateEpicTimings(int epicId) {
        Epic epic = epics.get(epicId);
        List<Integer> subs = epic.getSubtaskIds();
        if (subs.isEmpty()) {
            epic.setDuration(null);
            epic.setStartTime(null);
            epic.setEndTime(null);
            return;
        }
        Duration totalDuration = Duration.ZERO;
        LocalDateTime earliestStart = null;
        LocalDateTime latestEnd = null;

        for (int id : subs) {
            Subtask subtask = subtasks.get(id);
            if (subtask.getStartTime() != null && subtask.getDuration() != null) {
                totalDuration = totalDuration.plus(subtask.getDuration());

                LocalDateTime start = subtask.getStartTime();
                LocalDateTime end = subtask.getEndTime();

                if (earliestStart == null || (start != null && start.isBefore(earliestStart))) {
                    earliestStart = start;
                }

                if (latestEnd == null || (end != null && end.isAfter(latestEnd))) {
                    latestEnd = end;
                }
            }
        }

        epic.setDuration(totalDuration);
        epic.setStartTime(earliestStart);
        epic.setEndTime(latestEnd);

    }

    @Override
    public boolean isOverlapping(Task task1, Task task2) {
        return task1.getStartTime().isBefore(task2.getEndTime()) &&
                task2.getStartTime().isBefore(task1.getEndTime());
    }

    @Override
    public boolean hasOverlapWithAnyTask(Task taskToCheck) {
        return getPrioritizedTasks().stream()
                .filter(task -> task.getId() != taskToCheck.getId())
                .anyMatch(task -> isOverlapping(task, taskToCheck));
    }
}
