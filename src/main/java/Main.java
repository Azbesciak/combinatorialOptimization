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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

public class Main {
	private static Scanner scanner = new Scanner(System.in);
	private static final List<Double> EVAPORATION_LIST =
			new ArrayList<>(Arrays.asList(0.6,0.65));

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
		int iterations = 6;
		ExecutorService executorService = Executors.newWorkStealingPool();
		List<Callable<Object>> callables = new ArrayList<>(iterations);
		ConcurrentLinkedQueue<String> results = new ConcurrentLinkedQueue<>();
		for (int i = 0; i < iterations; i++) {
			final int iteration = i;
			callables.add(() -> makeAutomaticSolution(iteration, prefix + iteration, instance, 0L, results));
		}
		long start = System.currentTimeMillis();
		executorService.invokeAll(callables);
		long end = System.currentTimeMillis();

		String header = "params";
		for (double ratio : EVAPORATION_LIST) {
			header += " " + ratio;
		}
		header += " mediumTime[s] bestResult";
		SolutionService.persistSolutionsResults(results, header);

		System.out.println("TOTAL TIME : " + ((end - start) / (60.0 * 1000)) + " min");
	}

	private static Object makeAutomaticSolution(int iteration, String prefix, Instance instance, long endTime,
												ConcurrentLinkedQueue<String> queue) throws IOException {
		if ("0".equals(prefix)) {
			prefix = "AUTO_";
		}
		int iterations = getIterationsAmount(iteration);
		int antPopulation = getAntPopulation(iteration);
		String testParams = "It" + iterations + "An" + antPopulation;
		long totalTime = 0;
		int bestQuality = Integer.MAX_VALUE;
		long iterationTotalTime = 0;
		for (double evaporationRate : EVAPORATION_LIST){
			int roundQuality = 0;
			double rounds = 20.0;
			for (int i = 0; i < rounds; i++) {
				long start = System.currentTimeMillis();
				AntColonyAlgorithm antColonyAlgorithm = new AntColonyAlgorithm(iterations, endTime, antPopulation,
						evaporationRate, instance, prefix + Thread.currentThread().getId());
				Instance result = antColonyAlgorithm.run();
				long end = System.currentTimeMillis();
				iterationTotalTime += (end - start) / 1000.0;
				if (result.getQuality() < bestQuality) {
					bestQuality = result.getQuality();
				}
				roundQuality += result.getQuality();
			}
			double mediumTimeFromRound = iterationTotalTime / rounds;
			totalTime += mediumTimeFromRound;
			testParams += " " + roundQuality / rounds;
		}
		testParams += " " + (totalTime / (double) EVAPORATION_LIST.size());
		testParams += " "  + bestQuality;
		queue.add(testParams);
//		String instanceParams = "T" + instance.getTasks().size() + "_M" + instance.getMaintenances().size();
//		String customName = prefix + instanceParams + ";_It;" + iterations + ";_An;" +
//				antPopulation + ";_Ev;" + evaporationRatio + ";_Sp;" + solutionPersistenceAmount;
//		System.out.println(customName + " : " + (end - start) / 1000.0 + " sec");
//		SolutionService.persistSolution(result, customName);

//		int initialSchedulingTime = result.getInitialSchedulingTime();
//		int quality = result.getQuality();
//		String toWrite = customName + "; start;" + initialSchedulingTime + "; end; " +
//				quality + "; improved by; " + (100.0 * (1 - quality / (double) initialSchedulingTime)) + "%";

		return null;
	}

	private static int getIterationsAmount(int iteration) {
		return makeSwitchForInts(iteration);
	}

	private static int getAntPopulation(int iteration) {
		return 1000000 / makeSwitchForInts(iteration);
	}

	private static int makeSwitchForInts(int param) {
		switch (param) {
			case 0:
				return 100;
			case 1:
				return 250;
			case 2:
				return 500;
			case 3:
				return 1000;
			case 4:
				return 2000;
			default:
				return 4000;
		}
	}

	private static int getInstancesToPersistPerColonyTrail(int iteration) {
		int edge = iteration % 16;
		return (edge / 8 + 1);
	}

	private static void useInstanceToCustomSimulation(Instance instance) throws IOException {
		int iterations = 0;
		double timeInMinutes = 0;
		long simulationEndTime = 0L;
		System.out.println("limited by iterations or time [i/t]");
		String choose = scanner.next();
		if ("I".equalsIgnoreCase(choose)) {
			System.out.println("pass iterations amount");
			iterations = scanner.nextInt();
			if (iterations <= 0) {
				iterations = 1000;
			}
		} else {
			System.out.println("how long in minutes?");
			timeInMinutes = scanner.nextDouble();
			if (timeInMinutes <= 0) {
				timeInMinutes = 10;
			}
		}
		System.out.println("pass ants population amount");
		int amount = scanner.nextInt();
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

		System.out.println("Pass a prefix (0 - none)");
		String prefix = scanner.next();
		if ("0".equals(prefix)) {
			prefix = "";
		} else {
			prefix += "_";
		}
		if (timeInMinutes > 0) {
			simulationEndTime = (long) (timeInMinutes * 60 * 1000) + System.currentTimeMillis();
		}
		performSimulationAndSaveResults(iterations, simulationEndTime, antPopulation, evaporationRatio, instance,
				timeInMinutes, prefix);

	}

	private static int performSimulationAndSaveResults(int iterations, long endTime, int antPopulation, double evaporationRatio,
														Instance instance,
														double timeInMinutes,
														String prefix) throws IOException {
		AntColonyAlgorithm antColonyAlgorithm = new AntColonyAlgorithm(iterations,endTime, antPopulation, evaporationRatio,
				instance, prefix);
		long start = System.currentTimeMillis();
		Instance receivedInstance = antColonyAlgorithm.run();
		long end = System.currentTimeMillis();
		UtilsService.printPrettyJson(receivedInstance);

		String instanceParams = "T" + receivedInstance.getTasks().size() + "_M" + receivedInstance.getMaintenances()
				.size();
		String customName = prefix + instanceParams;
		if (iterations > 0) {
			customName += "_It" + iterations;
		} else {
			customName += "_Ti" + timeInMinutes;
		}
		customName += "_An" + antPopulation + "_Ev" + evaporationRatio;
		System.out.println("\n" + customName + " : " + (end - start) / 1000.0 + " sec");
		SolutionService.persistSolution(receivedInstance, customName);
		return receivedInstance.getQuality();
	}
}