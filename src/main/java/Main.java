import algorithm.AntColonyAlgorithm;
import model.wrapper.Instance;
import model.Maintenance;
import model.Task;
import repository.InstanceRepository;
import service.MaintenanceService;
import service.SolutionService;
import service.TaskService;
import service.UtilsService;

import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

public class Main {
	private static Scanner scanner = new Scanner(System.in);

	public static void main(String[] args) throws Exception {
		Instance instance;
		System.out.println(
				"create new instances or load existing ones? \n 0 - new \n 1 - existing \n 2 - automatic with selected Instance");
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
			case 2: {
				instance = useExistingInstancePath();
				System.out.println("please, input a prefix");
				String prefix = scanner.next();
				automatic(prefix, instance);
				break;
			}
			default:
				throw new RuntimeException();
		}
		if (answer != 2) {
			useInstanceToCustomSimulation(instance);
		}
	}

	private static Instance newInstancePath() throws Exception {
		System.out.println("Pass longest task duration");
		int ans = scanner.nextInt();
		int longestTime = ans == 0 ? 100 : ans;

		System.out.println("Pass task amount");
		ans = scanner.nextInt();
		int taskAmount = ans == 0 ? 100 : ans;

		System.out.println("Pass maintenances amount");
		ans = scanner.nextInt();
		int maintenancesAmount = ans == 0 ? taskAmount / 4 : ans;

		List<Task> tasks = TaskService.generateTasks(taskAmount, longestTime);
		int totalTime = TaskService.getTotalTasksDuration(tasks);

		List<Maintenance> maintenances = MaintenanceService
				.generateMaintenances(totalTime, maintenancesAmount, longestTime);
		Instance instance = new Instance(tasks, maintenances);
		System.out.println("would you like to persist? [y/n]");
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

	private static Instance useExistingInstancePath() throws Exception {
		List<Path> paths = InstanceRepository.listAllInstances();
		if (paths.size() == 0) {
			System.out.println("No existing files...");
			return newInstancePath();
		}
		System.out.println("Please, select one of listed below files");
		for (int i = 0; i < paths.size(); i++) {
			System.out.println(i + " -> " + paths.get(i).getFileName());
		}
		int index = scanner.nextInt();
		if (index >= 0 && index < paths.size()) {
			return InstanceRepository.getInstance(paths.get(index));
		} else {
			throw new RuntimeException();
		}
	}

	private static void automatic(String prefix, final Instance instance) throws Exception {
		ExecutorService executorService = Executors.newWorkStealingPool();
		List<Callable<String>> callables = new ArrayList<>(625);
		for (int i = 0; i < 3; i++) {
			final int iteration = i;
			callables.add(() -> makeSolution(iteration, prefix, instance));
		}
		long start = System.currentTimeMillis();
		List<Future<String>> futures = executorService.invokeAll(callables);

		long end = System.currentTimeMillis();
		SolutionService.persistSolutionsResults(futures);

		System.out.println("TOTAL TIME : " + new SimpleDateFormat("mm,ss").format((end - start) / (60.0 * 1000)) + " min");
	}

	private static String makeSolution(int iteration, String prefix, Instance instance) throws IOException {
		if ("null".equals(prefix)) {
			prefix = "AUTO_";
		}
		int iterations = getIterationsAmount(iteration);
		int antPopulation = getAntPopulation(iteration);
		double evaporationRatio = getEvaporation(iteration);
		int solutionPersistenceAmount = getInstancesToPersistPerColonyTrail(iteration);
		long start = System.currentTimeMillis();
		AntColonyAlgorithm antColonyAlgorithm = new AntColonyAlgorithm(iterations, antPopulation, evaporationRatio,
				instance,
				solutionPersistenceAmount);
		Instance result = antColonyAlgorithm.run();
		long end = System.currentTimeMillis();
		String instanceParams = "T" + instance.getTasks().size() + "_Ma" + instance.getMaintenances().size();
		String customName = prefix + instanceParams + "_It" + iterations + "_An" +
				antPopulation + "_Ev" + evaporationRatio + "_Sp" + solutionPersistenceAmount;
		System.out.println(customName + " : " + (end - start) / 1000.0 + " sec");
		SolutionService.persistSolution(result, customName);

		int initialSchedulingTime = result.getInitialSchedulingTime();
		int quality = result.getQuality();
		return customName + " start:" + initialSchedulingTime + ", end:" +
				quality + " improved by: " + (100.0 * (1 - quality / (double) initialSchedulingTime)) + "%";
	}

	private static int getIterationsAmount(int iteration) {
		return makeSwitchForInts(iteration % 5);
	}

	private static int getAntPopulation(int iteration) {
		int edge = iteration % 25;
		return makeSwitchForInts(edge / 5);
	}

	private static int makeSwitchForInts(int param) {
		switch (param) {
			case 0:
				return 10;
			case 1:
				return 50;
			case 2:
				return 100;
			case 3:
				return 500;
			default:
				return 1000;
		}
	}

	private static int getInstancesToPersistPerColonyTrail(int iteration) {
		int edge = iteration % 625;
		return edge / 125 + 1;
	}

	private static double getEvaporation(int iteration) {
		int edge = iteration % 125;
		switch (edge / 25) {
			case 0:
				return 0.6;
			case 1:
				return 0.7;
			case 2:
				return 0.8;
			case 3:
				return 0.9;
			default:
				return 0.95;
		}
	}

	private static void useInstanceToCustomSimulation(Instance instance) throws IOException {
		System.out.println("pass iterations amount");
		int amount = scanner.nextInt();
		int iterations = amount == 0 ? 100 : amount;

		System.out.println("pass ants population amount");
		amount = scanner.nextInt();
		int antPopulation = amount == 0 ? 100 : amount;

		double evaporationInput = 0;

		System.out.println("pass evaporationRatio per iteration amount");
		try {
			evaporationInput = scanner.nextDouble();
			if (evaporationInput > 1 || evaporationInput < 0) {
				System.out.println("WRONG value, used default");
				evaporationInput = 0;
			}
		} catch (Exception e) {
			System.out.println("WRONG value, used default");
		}
		double evaporationRatio = evaporationInput == 0 ? 0.7 : evaporationInput;

		System.out.println("pass solution persistence per iteration amount");
		amount = scanner.nextInt();
		int solutionPersistenceAmount = amount == 0 ? 5 : amount;

		long start = System.currentTimeMillis();
		AntColonyAlgorithm antColonyAlgorithm = new AntColonyAlgorithm(iterations, antPopulation, evaporationRatio,
				instance,
				solutionPersistenceAmount);
		Instance receivedInstance = antColonyAlgorithm.run();
		long end = System.currentTimeMillis();
		UtilsService.printPrettyJson(receivedInstance);

		String instanceParams = "T" + receivedInstance.getTasks().size() + "_M" + receivedInstance.getMaintenances()
				.size();
		String customName = instanceParams + "_It" + iterations + "_An" + antPopulation + "_Ev" + evaporationRatio + "_Sp" + solutionPersistenceAmount;
		System.out.println("\n" + customName + " : " + (end - start) / 1000.0 + " sec");
		SolutionService.persistSolution(receivedInstance, customName);
	}
}