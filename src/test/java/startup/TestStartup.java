package startup;

import model.Maintenance;
import model.Task;
import model.wrapper.Instance;
import service.MaintenanceService;
import service.TaskService;

import java.util.List;

public class TestStartup {

	public static Instance prepareTasks() throws Exception {
		int longestTime = 100;
		int taskAmount = 80;
		int maintenancesAmount = taskAmount / 4;
		List<Task> tasks = TaskService.generateTasks(taskAmount, longestTime, shortestTime);
		int totalTime = TaskService.getTotalTasksDuration(tasks);
		List<Maintenance> maintenances = MaintenanceService.generateMaintenances(totalTime, maintenancesAmount, 100);
		return new Instance(tasks, maintenances);
	}
}
