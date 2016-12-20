import model.wrapper.Instance;
import model.Maintenance;
import model.Task;
import repository.InstanceRepository;
import service.MaintenanceService;
import service.SolutionService;
import service.TaskService;

import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
	private static Scanner scanner = new Scanner(System.in);

	public static void main(String[] args) throws Exception {
		Instance instance;
		System.out.println("create new instances or load existing ones? \n 0 - new \n 1 - existing");
		int answer = scanner.nextInt();
		switch (answer) {
			case 0: {
				instance = newInstancePath();
				break;
			}
			case 1: {
				instance = useExistingInstancePath();
				break;
			}
			default: throw new RuntimeException();
		}
		List<Task> tasks = instance.getTasks();
		List<Maintenance> maintenances = instance.getMaintenances();
		AtomicInteger maintenanceLength = new AtomicInteger();
		int sum = maintenances.stream().mapToInt(Maintenance::getDuration).sum();

		List<Task> taskList = TaskService.randomGenerator(maintenances, tasks);
		instance.setTasks(taskList);
		SolutionService.persistSolution(instance, "test");
//		UtilsService.printPrettyJson(taskList);
	}

	private static Instance newInstancePath() throws Exception{
		int longestTime = 100;
		int taskAmount = 80;
		int maintenancesAmount = taskAmount / 4;
		List<Task> tasks = TaskService.generateTasks(taskAmount, longestTime);
		int totalTime = TaskService.getTotalTasksDuration(tasks);
		List<Maintenance> maintenances = MaintenanceService.generateMaintenances(totalTime, maintenancesAmount);
		Instance instance = new Instance(tasks, maintenances);
		System.out.println("would you want to persist? [y/n]");
		String persistAns = scanner.next();
		if ("y".equals(persistAns.toLowerCase())) {
			System.out.println("please, give a name or press 'n' for default name, and then enter");
			persistAns = scanner.next();
			if ("n".equals(persistAns.toLowerCase())) {
				InstanceRepository.persistInstance(instance);
			} else {
				InstanceRepository.persistInstance(instance, persistAns);
			}
		}
		return instance;
	}

	private static Instance useExistingInstancePath() throws Exception{
		List<Path> paths = InstanceRepository.listAllInstances();
		System.out.println("Please, select one of listed below files");
		for (int i = 0; i < paths.size(); i++) {
			System.out.println(i + " -> " + paths.get(i).getFileName());
		}
		int index = scanner.nextInt();
		if (index >= 0 && index < paths.size()) {
			return InstanceRepository.getInstance(paths.get(index));
		} else throw new RuntimeException();
	}
}
//TODO upgrade tests