import model.Maintenance;
import model.Task;
import service.MaintenanceService;
import service.TaskService;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws Exception {
        int longestTime = 100;
        int taskAmount = 80;
        int maintenancesAmount = taskAmount / 4;
        List<Task> tasks = TaskService.generateTasks(taskAmount, longestTime);
        int totalTime = TaskService.getTotalTasksDuration(tasks);
        List<Maintenance> maintenances = MaintenanceService.generateMaintenances(totalTime, maintenancesAmount);
        AtomicInteger maintenanceLength = new AtomicInteger();
        maintenances.stream().parallel().forEach(t -> maintenanceLength.getAndAdd(t.getDuration()));
		List<Task> taskList = TaskService.randomGenerator(maintenances, tasks);
		System.out.println(taskList);
	}
}
//TODO check begin time of each operation depending on the machine.
//TODO upgrade tests