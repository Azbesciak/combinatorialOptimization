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
import java.util.*;
import java.util.concurrent.*;

public class Main {
	private static Scanner scanner = new Scanner(System.in);
	private static final List<Double> EVAPORATION_LIST =
			new ArrayList<>(Arrays.asList(0.6, 0.65, 0.7, 0.75, 0.8, 0.85, 0.9, 0.95));

	private static final List<Integer> SOLUTION_PERSISTENCE_LIST =
			new ArrayList<>(Arrays.asList(15,20,25,30));

	private static final List<Integer> INDEPENDENCE_RATE_LIST =
			new ArrayList<>(Arrays.asList(-1, 0, 5, 10, 15, 20));

	public static void main(String[] args) throws Exception {
		Instance instance = null;
		System.out.println(
				"create new instances or load existing ones? \n" +
						" 0 - new \n" +
						" 1 - existing \n" +
						" 2 - automatic with selected Instance (SP)\n" +
						" 3 - Instances test (DEV)\n" +
						" 4 - Task amount comparison (DEV)");
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
			case 3: {
				makeTestForInstances();
				break;
			}
			case 4: {
				makeInstancesComparison();
				break;
			}
			default:
				throw new RuntimeException();
		}
		if (answer < 2) {
			useInstanceToCustomSimulation(instance);
		}
	}

	private static void makeInstancesComparison() throws Exception {
		List<Instance> instances = new ArrayList<>();
		List<String> names = new ArrayList<>();
		ConcurrentLinkedQueue<String> results = new ConcurrentLinkedQueue<>();
		int longestTime = 150;
		int shortestTime = 1;
		int taskAmount = 50;
		int maintenanceDuration = 150;
		String prefix = "DifferentTasksComparison";
		for (int i = 1; i <= 36; i++) {
			int tasksSize = taskAmount * (1 + (i % 4));
			int maintenancesAmount = (int)(tasksSize * ((5 + (i % 3)) * 0.05));
			Instance instance = newInstancePath(longestTime, shortestTime, tasksSize, maintenancesAmount, maintenanceDuration);
			String name = i + prefix + "T" + tasksSize + "tt" +
					shortestTime + "_" + longestTime + "M" + maintenancesAmount + "mt" ;
			if (maintenanceDuration == 0) {
				name += "Def";
			} else {
				name += maintenanceDuration;
			}
			InstanceRepository.persistInstance(instance, name);
			instances.add(instance);
			names.add(name);
		}
		final double ev = 0.75;
		ExecutorService executorService = Executors.newWorkStealingPool();
		List<Callable<Object>> callables = new ArrayList<>();
		for (int i = 0; i < instances.size(); i++) {
			final int index = i;
			callables.add(() -> makeTestWithParam(2000, ev, instances.get(index),
					results, names.get(index)));
		}
		long start = System.currentTimeMillis();
		executorService.invokeAll(callables);
		long end = System.currentTimeMillis();
		System.out.println("\nTOTAL TIME = " + (end - start) / 1000.0 + " sec");
		SolutionService.persistSolutionsResults(results, null);
	}

	private static void makeTestForInstances() throws Exception {
		System.out.println("Pass longest task duration");
		int ans = scanner.nextInt();
		int longestTime = ans == 0 ? 100 : ans;

		System.out.println("Pass the minimum duration of the task");
		ans = scanner.nextInt();
		int shortestTime = ans <= 0 ? 1 : ans;

		System.out.println("Pass task amount");
		ans = scanner.nextInt();
		int taskAmount = ans == 0 ? 100 : ans;

		System.out.println("Pass maintenances amount");
		ans = scanner.nextInt();
		int maintenancesAmount = ans == 0 ? taskAmount / 4 : ans;

		System.out.println("Pass maintenance longest duration (0 - default)");
		int maintenanceDuration = scanner.nextInt();

		System.out.println("prefix?");
		String prefix = scanner.next();
		if ("0".equals(prefix)) {
			prefix = "";
		}
		List<Instance> instances = new ArrayList<>();
		List<String> names = new ArrayList<>();
		ConcurrentLinkedQueue<String> results = new ConcurrentLinkedQueue<>();
		for (int i = 0; i < 10; i++) {
			Instance instance = newInstancePath(longestTime, shortestTime, taskAmount, maintenancesAmount, maintenanceDuration);
			String name = i + prefix + "T" + taskAmount + "tt" +
					shortestTime + "_" + longestTime + "M" + maintenancesAmount + "mt" ;
			if (maintenanceDuration == 0) {
				name += "Def";
			} else {
				name += maintenanceDuration;
			}
			InstanceRepository.persistInstance(instance, name);
			instances.add(instance);
			names.add(name);
		}
		final double ev = 0.75;
		ExecutorService executorService = Executors.newWorkStealingPool();
		List<Callable<Object>> callables = new ArrayList<>();
		for (int i = 0; i < instances.size(); i++) {
			final int index = i;
				callables.add(() -> makeTestWithParam(2000, ev, instances.get(index),
						results, names.get(index)));
		}
		long start = System.currentTimeMillis();
		executorService.invokeAll(callables);
		long end = System.currentTimeMillis();
		System.out.println("\nTOTAL TIME = " + (end - start) / 1000.0 + " sec");
		SolutionService.persistSolutionsResults(results, null);
	}

	private static Object makeTestWithParam(int iterations, double evaporationRate, Instance instance,
											ConcurrentLinkedQueue<String> results, String name)
			throws IOException {
		AntColonyAlgorithm antColonyAlgorithm = new AntColonyAlgorithm(iterations,
				0, 1000, 1,  evaporationRate, instance,
				name );
		Instance result = antColonyAlgorithm.run();
		SolutionService.persistSolution(result, name);
		results.add(name + " " + result.getQuality()+ " " + (1 - result.getQuality() / (double) result.getInitialSchedulingTime())); //
		return null;
	}

	private static Instance newInstancePath() throws Exception {
		System.out.println("Pass the maximum duration of the task");
		int ans = scanner.nextInt();
		int longestTime = ans == 0 ? 100 : ans;

		System.out.println("Pass the minimum duration of the task");
		ans = scanner.nextInt();
		int shortestTime = ans <= 0 ? 1 : ans;

		System.out.println("Pass task amount");
		ans = scanner.nextInt();
		int taskAmount = ans == 0 ? 100 : ans;

		System.out.println("Pass maintenances amount");
		ans = scanner.nextInt();
		int maintenancesAmount = ans == 0 ? taskAmount / 4 : ans;

		System.out.println("Pass the maximum duration of the maintenance (0 - default)");
		int maintenanceDuration = scanner.nextInt();

		Instance instance = newInstancePath(longestTime, shortestTime, taskAmount, maintenancesAmount, maintenanceDuration);
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

	private static Instance newInstancePath(int longestTime, int shortestTime, int taskAmount, int maintenancesAmount,
											int maxMaintenanceTime) throws Exception {

		List<Task> tasks = TaskService.generateTasks(taskAmount, longestTime, shortestTime);
		int totalTime = TaskService.getTotalTasksDuration(tasks);

		List<Maintenance> maintenances = MaintenanceService
				.generateMaintenances(totalTime, maintenancesAmount, longestTime, maxMaintenanceTime);
		return new Instance(tasks, maintenances);
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
		int iterations = 5;
		ExecutorService executorService = Executors.newWorkStealingPool();
		List<Callable<Object>> callables = new ArrayList<>(iterations);
		ConcurrentLinkedQueue<String[]> results = new ConcurrentLinkedQueue<>();
		for(int sp : SOLUTION_PERSISTENCE_LIST) {
			callables.add(() -> makeAutomaticSolutionForSp(sp, prefix, instance, 0, results));
		}
		long start = System.currentTimeMillis();
		executorService.invokeAll(callables);
		long end = System.currentTimeMillis();
		List<String> solutionPersistence = prepareResultFromArrays(results, "solutionPersistence");
		SolutionService.persistSolutionsResults(solutionPersistence, "");

		System.out.println("TOTAL TIME : " + ((end - start) / (60.0 * 1000)) + " min");
	}

	private static Object makeAutomaticSolutionForEv(int iteration, String prefix, Instance instance, long endTime,
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
						1, evaporationRate, instance, prefix + Thread.currentThread().getId());
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
		return null;
	}
	private static List<String> prepareResultFromArrays(Queue<String[]> listOfArrays, String name) {
		List<String> result = new ArrayList<>();
		String header = name + listOfArrays.peek()[0];
		String details = "mediumQuality";
		String mediumTime = "mediumTIme";
		String bestResults = "bestResults";
		for(String[] arr : listOfArrays) {
			if (arr.length >= 5) {
				header += " " + arr[1];
				details += " " + arr[2];
				mediumTime += " " + arr[3];
				bestResults += " " + arr[4];
			}
		}
		result.add(header);
		result.add(details);
		result.add(mediumTime);
		result.add(bestResults);
		return result;
	}

	private static Object makeAutomaticSolutionForSp(int sp, String prefix, Instance instance, long endTime,
												ConcurrentLinkedQueue<String[]> queue) throws IOException {
		String[] params = new String[5];
		int iterations = 2000;
		int antPopulation = 1000;
		double evaporationRate = 0.75;
		String testParams = "It" + iterations + "An" + antPopulation;
		params[0] = testParams;
		params[1] = String.valueOf(sp);
		int bestQuality = Integer.MAX_VALUE;
		long iterationsTotalTime = 0;
		int roundQuality = 0;
		double rounds = 20.0;
		for (int i = 0; i < rounds; i++) {
			long start = System.currentTimeMillis();
			AntColonyAlgorithm antColonyAlgorithm = new AntColonyAlgorithm(iterations, endTime, antPopulation,
					sp, evaporationRate , instance, prefix + Thread.currentThread().getId());
			Instance result = antColonyAlgorithm.run();
			long end = System.currentTimeMillis();
			iterationsTotalTime += (end - start) / 1000.0;
			roundQuality += result.getQuality();
			if (result.getQuality() < bestQuality) {
				bestQuality = result.getQuality();
			}
			String instanceParams = "T" + instance.getTasks().size() + "_M" + instance.getMaintenances().size();
			String customName = prefix + instanceParams + "_It" + iterations + "_An" +
					antPopulation  + "_SP" + sp;
			SolutionService.persistSolution(result, customName);
		}
		params[2] = String.valueOf(roundQuality / rounds);
		params[3] = String.valueOf(iterationsTotalTime / rounds);
		params[4] = String.valueOf(bestQuality);
		queue.add(params);
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
				return 500;
			case 2:
				return 1000;
			case 3:
				return 2000;
			default:
				return 5000;
		}
	}

	private static int getInstancesToPersistPerColonyTrail(int iteration) {
		int edge = iteration % 16;
		return (edge / 8 + 1);
	}

	private static void useInstanceToCustomSimulation(Instance instance) throws Exception {
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

		System.out.println("pass evaporationRate per iteration amount");
		try {
			evaporationInput = scanner.nextDouble();
			if (evaporationInput > 1 || evaporationInput < 0) {
				System.out.println("WRONG value, used default");
				evaporationInput = 0;
			}
		} catch (Exception e) {
			System.out.println("WRONG value, used default");
		}
		double evaporationRate = evaporationInput == 0 ? 0.7 : evaporationInput;

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
		performSimulationAndSaveResults(iterations, simulationEndTime, antPopulation, evaporationRate, instance,
				timeInMinutes, prefix);

	}

	private static int performSimulationAndSaveResults(int iterations, long endTime, int antPopulation,
													   double evaporationRate,
													   Instance instance,
													   double timeInMinutes,
													   String prefix) throws Exception {
		AntColonyAlgorithm antColonyAlgorithm = new AntColonyAlgorithm(iterations, endTime, antPopulation,
				0, evaporationRate,
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
		customName += "_An" + antPopulation + "_Ev" + evaporationRate;
		System.out.println("\n" + customName + " : " + (end - start) / 1000.0 + " sec");
		SolutionService.persistSolution(receivedInstance, customName);
		return receivedInstance.getQuality();
	}
}