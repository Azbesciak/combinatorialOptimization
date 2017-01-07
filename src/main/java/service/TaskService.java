package service;

import exception.NonNegativeArgException;
import model.wrapper.Instance;
import model.Maintenance;
import model.Operation;
import model.Task;

import java.util.*;

import static service.OperationsService.*;

public class TaskService {

	private TaskService() {
		throw new UnsupportedOperationException();
	}

	public static List<Task> generateTasks(int amount, int longestTime, int shortestTime) throws NonNegativeArgException {
		if (longestTime <= 0) {
			throw new NonNegativeArgException("Task time must be positive!");
		}
		if (amount <= 0) {
			throw new NonNegativeArgException("Task amount must be positive!");
		}
		if (shortestTime < 0) {
			shortestTime = 1;
		}
		Random random = new Random();
		int maxReadyTime = (int) ((amount * longestTime) * 0.2);
		List<Task> tasks = new ArrayList<>();
		Task.resetIndexer();
		for (int i = 0; i < amount; i++) {
			int firstTaskDuration = getTaskDuration(shortestTime, longestTime);
			int readyTime = random.nextBoolean() ? random.nextInt(maxReadyTime) : 0;
			Operation first = Operation.createFirstMachineOperation(firstTaskDuration, readyTime);

			int secondTimeDuration = getTaskDuration(shortestTime, longestTime);
			Operation second = Operation.createSecondMachineOperation(secondTimeDuration);

			Task task = new Task(first, second);
			tasks.add(task);
		}
		Task.resetIndexer();
		return tasks;
	}
	private static int getTaskDuration(int minDuration, int maxDuration) {
		return new Random().nextInt((maxDuration - minDuration)) + minDuration;
	}

	public static int getTotalTasksDuration(List<Task> tasks) {
		int totalTime = 0;
		if (tasks != null) {
			for (Task task : tasks) {
				totalTime += Math.max(task.getFirst().getDuration(), task.getSecond().getDuration());
			}
		}
		return totalTime;
	}

	public static List<Task> randomGenerator(final Instance instance) {
		return randomGenerator(instance.getMaintenances(), instance.getTasks());
	}

	public static List<Task> randomGenerator(final List<Maintenance> maintenances, final List<Task> tasks) {

		List<Maintenance> machineFirstMaintenances = UtilsService.deepClone(maintenances);
		List<Task> tasksCopy = UtilsService.deepClone(tasks);
		Collections.shuffle(tasksCopy);
		prepareFirstMachineOperations(tasksCopy, machineFirstMaintenances);
		prepareSecondMachineOperations(tasksCopy);

		return tasksCopy;
	}
}
