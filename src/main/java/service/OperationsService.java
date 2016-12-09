package service;

import enumeration.Machine;
import model.Maintenance;
import model.Operation;
import model.Task;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class OperationsService {
	public static void assignOperationsToMachines(List<Task> tasks) {
		Random random = new Random();
		for (Task task : tasks) {
			boolean roulette = random.nextBoolean();
			task.getFirst().setMachine(roulette ? Machine.ONE : Machine.TWO);
			task.getSecond().setMachine(roulette ? Machine.TWO : Machine.ONE);
		}
	}

	public static void prepareFirstMachineOperations(List<Task> tasks, List<Maintenance> machineOneMaintenaces) {
		Iterator<Maintenance> maintenanceIterator = machineOneMaintenaces.iterator();
		Maintenance nearestMaintenance = maintenanceIterator.next();
		int lastOperationEndTime = -1;
		for (Task task : tasks) {
			Operation operation;
			if (Machine.ONE.equals(task.getFirst().getMachine())) {
				operation = task.getFirst();
			} else {
				operation = task.getSecond();
			}
			lastOperationEndTime = Math.max(lastOperationEndTime, operation.getReadyTime() - 1);

			while (nearestMaintenance != null && lastOperationEndTime > nearestMaintenance.getBegin()) {
				nearestMaintenance = nextIteratorsObject(maintenanceIterator);
			}

			if (nearestMaintenance != null &&
					nearestMaintenance.getBegin() <= lastOperationEndTime + operation.getDuration() + 1) {
				lastOperationEndTime = nearestMaintenance.getEnd();
				nearestMaintenance = nextIteratorsObject(maintenanceIterator);

			}
			operation.startOperation(lastOperationEndTime + 1);
			lastOperationEndTime = operation.getEnd();
		}
	}

	public static void prepareSecondMachineOperations(List<Task> tasks) {
		int lastOperationEndTime = -1;
		for (Task task : tasks) {
			Operation operation;
			int firstMachineLastOperationEndTime;
			if (Machine.TWO.equals(task.getFirst().getMachine())) {
				operation = task.getFirst();
				firstMachineLastOperationEndTime = task.getSecond().getEnd();
			} else {
				operation = task.getSecond();
				firstMachineLastOperationEndTime = task.getFirst().getEnd();
			}
			lastOperationEndTime = Math.max(lastOperationEndTime, firstMachineLastOperationEndTime);

			lastOperationEndTime = operation.
					startOperationWhenPossibleAndGetEnd(lastOperationEndTime + 1);
		}
	}

	private static <E> E nextIteratorsObject(Iterator<E> iterator) {
		if (iterator.hasNext()) {
			return iterator.next();
		} else {
			return null;
		}
	}
}
