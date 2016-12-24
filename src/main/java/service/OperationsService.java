package service;

import enumeration.Machine;
import model.Maintenance;
import model.Operation;
import model.Task;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class OperationsService {

	public static void prepareFirstMachineOperations(List<Task> tasks, List<Maintenance> machineOneMaintenances) {
		Iterator<Maintenance> maintenanceIterator = machineOneMaintenances.iterator();
		Maintenance nearestMaintenance = maintenanceIterator.next();
		int lastOperationEndTime = 0;
		for (Task task : tasks) {
			Operation operation = getTaskOperationForMachine(task, Machine.ONE);
			lastOperationEndTime = Math.max(lastOperationEndTime, operation.getReadyTime());

			while (nearestMaintenance != null &&
					lastOperationEndTime + operation.getDuration() >= nearestMaintenance.getBegin()) {
				lastOperationEndTime = nearestMaintenance.getEnd();
				nearestMaintenance = nextIteratorsObject(maintenanceIterator);
			}
			operation.startOperation(lastOperationEndTime);
			lastOperationEndTime = operation.getEnd();
		}
	}

	public static void prepareSecondMachineOperations(List<Task> tasks) {
		int lastOperationEndTime = 0;
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
					startOperationWhenPossibleAndGetEnd(lastOperationEndTime);
		}
	}

	public static int startTimeAfterRecentEnd(int lastOperationEndTime) {
		return lastOperationEndTime + 1;
	}

	public static Operation getTaskOperationForMachine(Task task, Machine machine) {
		if (machine.equals(task.getFirst().getMachine())) {
			return task.getFirst();
		} else {
			return task.getSecond();
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
