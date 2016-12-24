package service;

import model.Maintenance;
import model.Task;
import model.wrapper.Instance;

import java.util.List;

public class InstanceService {

	public static Instance prepareInstance(final Instance instance) {
		return prepareInstance(instance.getTasks(), instance.getMaintenances());
	}

	public static Instance prepareInstance(List<Task> tasks, List<Maintenance> maintenances) {
		OperationsService.prepareFirstMachineOperations(tasks, maintenances);
		OperationsService.prepareSecondMachineOperations(tasks);
		return new Instance(tasks, maintenances);
	}

}
