package service;

import exception.NonNegativeArgException;
import model.Machine;
import model.Maintenance;
import model.Operation;
import model.Task;

import java.util.*;

public class TaskService {

	public static List<Task> generateTasks(int amount, int longestTime) throws NonNegativeArgException {
		if (longestTime <= 0) {
			throw new NonNegativeArgException("Task time must be positive!");
		}
		if (amount <= 0) {
			throw new NonNegativeArgException("Task amount must be positive!");
		}
		List<Task> tasks = new ArrayList<>();
		for (int i = 0; i < amount; i++) {

			int firstTaskDuration = new Random().nextInt((longestTime - 1)) + 1;
			Operation first = new Operation(firstTaskDuration);

			int secondTimeDuration = new Random().nextInt((longestTime - 1)) + 1;
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

	public static List<Machine> randomGenerator(final List<Maintenance> maintenances, final List<Task> tasks) {
		List<Machine> machines = new ArrayList<>(2);

		List<Maintenance> machineFirstMaintenances = UtilsService.deepClone(maintenances);
		List<Task> tasksCopy = UtilsService.deepClone(tasks);
		Collections.shuffle(tasksCopy);
		Queue<Operation> awaitingOperations = new LinkedList<>();

		List<Operation> machineFirstOperations = prepareFirstMachineOperations(
				tasksCopy, machineFirstMaintenances, awaitingOperations);
		List<Operation> machineTwoOperations = prepareSecondMachineOperations(machineFirstOperations,
				awaitingOperations);


		Machine machineOne = new Machine(machineFirstOperations, machineFirstMaintenances);
		Machine machineTwo = new Machine(machineTwoOperations, null);
		machines.add(machineOne);
		machines.add(machineTwo);
		return machines;
	}

	private static List<Operation> prepareFirstMachineOperations(List<Task> tasks,
																 List<Maintenance> machineOneMaintenaces,
																 Queue<Operation> awaitingOperations) {
		List<Operation> machineOneOperations = new LinkedList<>();

		Iterator<Maintenance> maintenanceIterator = machineOneMaintenaces.iterator();
		Maintenance nearestMaintenance = maintenanceIterator.next();
		Random random = new Random();
		int lastOperationEndTime = 0;
		for (Task task : tasks) {
			Operation operation;
			if (random.nextBoolean()) {
				operation = task.getFirst();
				awaitingOperations.add(task.getSecond());
			} else {
				operation = task.getSecond();
				awaitingOperations.add(task.getFirst());
			}
			if (nearestMaintenance != null &&
					nearestMaintenance.getBegin() <= lastOperationEndTime + operation.getDuration()) {
				lastOperationEndTime = nearestMaintenance.getEndTime() + 1;
				if (maintenanceIterator.hasNext()) {
					nearestMaintenance = maintenanceIterator.next();
				} else {
					nearestMaintenance = null;
				}
			}
			operation.startOperation(lastOperationEndTime);
			lastOperationEndTime = operation.getEndTime() + 1;
			machineOneOperations.add(operation);
		}
		return machineOneOperations;
	}

	private static List<Operation> prepareSecondMachineOperations(
			List<Operation> firstMachineOperations, Queue<Operation> operationsToDo) {

		List<Operation> secondMachineOperations = new LinkedList<>();
		int lastOperationEndTime = 0;
		int size = operationsToDo.size();
		for (int i = 0; i < size && !operationsToDo.isEmpty(); i++) {
			Operation firstMachineOperation = firstMachineOperations.get(i);
			lastOperationEndTime = Math.max(lastOperationEndTime, firstMachineOperation.getEndTime());
			Operation operationToDo = operationsToDo.poll();
			operationToDo.startOperation(lastOperationEndTime);
			lastOperationEndTime = operationToDo.getEndTime() + 1;
			secondMachineOperations.add(operationToDo);
		}
		return secondMachineOperations;
	}


}
