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

	public static List<Task> generateTasks(int amount, int longestTime) throws NonNegativeArgException {
		if (longestTime <= 0) {
			throw new NonNegativeArgException("Task time must be positive!");
		}
		if (amount <= 0) {
			throw new NonNegativeArgException("Task amount must be positive!");
		}
		Random random = new Random();
		int maxReadyTime = (int)((amount * longestTime) * 0.2);
		List<Task> tasks = new ArrayList<>();
		for (int i = 0; i < amount; i++) {

			int firstTaskDuration = random.nextInt((longestTime - 1)) + 1;
			int readyTime = random.nextInt(maxReadyTime);
			Operation first = new Operation(firstTaskDuration, readyTime);

			int secondTimeDuration = random.nextInt((longestTime - 1)) + 1;
			Operation second = new Operation(secondTimeDuration);

			Task task = new Task(first, second);
			tasks.add(task);
		}
		return tasks;
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
		tasksCopy.sort(Comparator.comparingInt(a -> a.getFirst().getReadyTime()));
		assignOperationsToMachines(tasksCopy);
		prepareFirstMachineOperations(tasksCopy, machineFirstMaintenances);
		prepareSecondMachineOperations(tasksCopy);

		return tasksCopy;
	}
}
