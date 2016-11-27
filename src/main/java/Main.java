import exception.NonNegativeArgException;
import model.Maintenance;
import model.Task;
import service.MaintenanceService;
import service.TaskService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) throws Exception {
        int longestTime = 15;
        int taskAmount = 75;
        List<Task> tasks = TaskService.generateTasks(taskAmount, longestTime);
        int totalTime = TaskService.getTotalTasksDuration(tasks);
        List<Maintenance> maintenances = MaintenanceService.generateMaintenances(totalTime, taskAmount);
        System.out.println(tasks + "\n" + maintenances);
    }
}
