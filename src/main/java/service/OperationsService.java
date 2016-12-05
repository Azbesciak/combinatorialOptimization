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
		int lastOperationEndTime = 0;
		for (Task task : tasks) {
			Operation operation;
			if (task.getFirst().getMachine() == Machine.ONE) {
				operation = task.getFirst();
			} else {
				operation = task.getSecond();
			}
			if (nearestMaintenance != null &&
					nearestMaintenance.getBegin() <= lastOperationEndTime + operation.getDuration()) {
				lastOperationEndTime = nearestMaintenance.getEnd() + 1;
				if (maintenanceIterator.hasNext()) {
					nearestMaintenance = maintenanceIterator.next();
				} else {
					nearestMaintenance = null;
				}
			}
			operation.startOperation(lastOperationEndTime);
			lastOperationEndTime = operation.getEnd() + 1;
		}
	}

	public static void prepareSecondMachineOperations(List<Task> tasks) {
		int lastOperationEndTime = 0;
		for (Task task : tasks) {
			Operation operation;
			int firstMachineLastOperationEndTime;
			if (task.getFirst().getMachine() == Machine.TWO) {
				operation = task.getFirst();
				firstMachineLastOperationEndTime = task.getSecond().getEnd();
			} else {
				operation = task.getSecond();
				firstMachineLastOperationEndTime = task.getSecond().getEnd();
			}
			lastOperationEndTime = Math.max(lastOperationEndTime, firstMachineLastOperationEndTime);
			operation.startOperation(lastOperationEndTime + 1);
		}
	}
}
