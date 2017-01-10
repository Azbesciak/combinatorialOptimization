package algorithm;

import model.Task;
import model.wrapper.Instance;
import service.UtilsService;

import java.io.IOException;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Collectors;

public class AntColonyAlgorithm {
	//	private final static String CHART_FOLDER = "testDir/chartData";
	private final static String CHART_FOLDER = "chartData";
	private final static double RANDOM_SOLUTIONS_EDGE = 0.25;
	public static final double PROGRESS_RATE = 0.9995;
	private final int SOLUTION_PERSISTENCE;

	private static final int PATH_EXPLORATION_REQ = 8;
	private static final double SEMI_MATRIX_SOLUTION_EDGE = 0.7;
	private static final int SEMI_MATRIX_SOLUTION_RANDOM_SCALE = 15;
	private final static int SMALL_ITERATION_BORDER = 100;
	private final static int STAGNATION_BORDER = 100;
	private final static double TABOO_SIZE_ITERATIONS_PERCENTAGE = 0.03;
	private final int AMNESIA_REQUIREMENT;
	private final Logger logger;
	private final PheromoneMatrix pheromoneMatrix;
	private final int iterations;
	private final long endTime;
	private final int antPopulation;
	private final Instance instance;
	private final int independenceRatio;


	public AntColonyAlgorithm(final int iterations, long endTime, final int antPopulation, final int sp,
							  final double evaporationRate, final Instance instance, String prefix) {
		this.iterations = iterations;
		this.endTime = endTime;
		this.antPopulation = antPopulation;
		this.instance = instance;
		this.independenceRatio = 0;
		this.SOLUTION_PERSISTENCE = sp;
		this.pheromoneMatrix = new PheromoneMatrix(instance.getTasks().size(), evaporationRate);
		logger = Logger.getLogger(this.toString());
		logger.setUseParentHandlers(false);
		AMNESIA_REQUIREMENT = (int) Math.min(Math.max(iterations * TABOO_SIZE_ITERATIONS_PERCENTAGE * antPopulation,
														antPopulation), 30 * 1000);
		System.setProperty("java.util.logging.SimpleFormatter.format", "%5$s%6$s%n");
		try {
			String savePathForNewFile = UtilsService
					.getSavePathForNewFile(CHART_FOLDER,
							prefix + "It" + iterations + "An" + antPopulation + "Sp" + SOLUTION_PERSISTENCE);
			FileHandler fh = new FileHandler(savePathForNewFile + ".log");
			logger.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
	}

	public Instance run(){
		final Set<List<Task>> alreadyDiscovered = new HashSet<>();
		Instance currentPath = randomIteration();
		Instance bestPath = null;
		Instance instanceToShow = currentPath;
		logger.info(1 + " " + instanceToShow.getQuality());
		int localOptimumDefender = 0;
		int initialSchedulingTime = currentPath.getQuality();
		int resetMoment = 0;
		long start = System.currentTimeMillis();
		for (int iteration = 1; iteration < iterations || System.currentTimeMillis() < endTime; iteration++) {
			if (iteration % 10 == 0 ) {
				logger.info(iteration + " " + instanceToShow.getQuality());
				String toShow = String.valueOf(currentPath.getQuality());
				if (bestPath != null) {
					toShow += " \033[33m" + bestPath.getQuality();
				}
				if (endTime > 0) {
					UtilsService.showProgress(Math.toIntExact(System.currentTimeMillis() - start),
							Math.toIntExact(endTime - start), toShow);
				} else {
					UtilsService.showProgress(iteration, iterations, toShow);
				}
			}
			Instance result = makeSmartIteration(iteration , resetMoment,
					iterations, currentPath, alreadyDiscovered, endTime != 0);
			if (result.getQuality() < instanceToShow.getQuality()) {
				instanceToShow = result;
			}
			if (result.getQuality() / (double) currentPath.getQuality() >= PROGRESS_RATE) {
				if (localOptimumDefender >= STAGNATION_BORDER && (iteration < 0.9 * iterations || endTime > 0)) {
					if ((bestPath != null && bestPath.getQuality() > currentPath.getQuality()) || bestPath == null) {
						bestPath = currentPath;
					}
					currentPath = randomIteration();
					iteration++;
					localOptimumDefender = 0;
					resetMoment = iteration;
					pheromoneMatrix.resetMatrix();
					pheromoneMatrix.updateMatrix(currentPath.getTasks(), currentPath.getQuality());
				} else {
					localOptimumDefender++;
					if (result.getQuality() < currentPath.getQuality()) {
						currentPath = result;
					}
				}
			} else if (result.getQuality() < currentPath.getQuality()) {
				currentPath = result;
				localOptimumDefender = 0;
			} else {
				localOptimumDefender = 0;
			}
		}
		if (bestPath != null && bestPath.getQuality() < currentPath.getQuality()) {
			currentPath = bestPath;
		}
		currentPath.setInitialSchedulingTime(initialSchedulingTime);
		Collections.sort(currentPath.getTasks());
		return currentPath;
	}

	private Instance makeSmartIteration(int iterationNumber, int resetMoment, int totalIterations, Instance bestResult,
										Set<List<Task>> alreadyDiscovered, boolean isTimeLimited) {
		int smallIteration = (iterationNumber + 1 - resetMoment) % SMALL_ITERATION_BORDER;
		double independenceRatio = this.independenceRatio == -1 ?
				getModularizedIndependenceRatio(smallIteration, iterationNumber < SMALL_ITERATION_BORDER) :
				this.independenceRatio;
		boolean smallIterationReset = smallIteration == 0;
		Set<Instance> instances;
		if (iterationNumber % PATH_EXPLORATION_REQ == 0) {
			if (AMNESIA_REQUIREMENT <= alreadyDiscovered.size()) {
				alreadyDiscovered.clear();
			}
			instances = extendTheBestSolution(bestResult, alreadyDiscovered);
		} else {
			instances = goForResearch(independenceRatio);
		}
		boolean shouldResetMatrix = smallIterationReset && (iterationNumber < totalIterations - 1 || isTimeLimited);
		Instance instance = persistAndGet(instances);
		if (shouldResetMatrix && instance.getQuality() > bestResult.getQuality()) {

			pheromoneMatrix.updateMatrix(bestResult.getTasks(), bestResult.getQuality());
		}
		return instance;
	}


	private double getModularizedIndependenceRatio(int condition, boolean isFirstCycle) {
		if (condition < RANDOM_SOLUTIONS_EDGE * SMALL_ITERATION_BORDER) {
			if (isFirstCycle) {
				return 100;
			} else {
				return 70;
			}
		} else if (condition < SEMI_MATRIX_SOLUTION_EDGE * SMALL_ITERATION_BORDER) {
			return SEMI_MATRIX_SOLUTION_RANDOM_SCALE;
		} else {
			return 0;
		}
	}

	private Instance randomIteration() {
		double independenceRatio = 100;
		Set<Instance> instances = goForResearch(independenceRatio);
		return persistAndGet(instances);
	}

	private Set<Instance> goForResearch(double independenceRatio) {
		Instance bestResult = null;
		TreeSet<Instance> instances = new TreeSet<>();
		for (int i = 0; i < antPopulation; i++) {
			Instance result = expeditionsAntOnAJourney(independenceRatio);
			instances.add(result);
			if (bestResult == null || bestResult.getQuality() > result.getQuality()) {
				bestResult = result;
			}
		}
		return instances;
	}

	private Set<Instance> extendTheBestSolution(Instance bestResult, Set<List<Task>> alreadyDiscovered) {
		alreadyDiscovered.add(bestResult.getTasks());
		TreeSet<Instance> instances = new TreeSet<>();
		for (int i = 0; i < antPopulation; i++) {
			Instance result = expeditionsAntOnADiscovery(bestResult.getTasks(), alreadyDiscovered);
			instances.add(result);
			if (result.getQuality() < bestResult.getQuality()) {
				bestResult = result;
			}
		}
		return instances;
	}

	private Instance persistAndGet(Set<Instance> results) {
		pheromoneMatrix.evaporateMatrix();
		List<Instance> collect = results.stream().limit(SOLUTION_PERSISTENCE).collect(Collectors.toList());
		for (Instance inst : collect) {
			pheromoneMatrix.updateMatrix(inst.getTasks(), inst.getQuality());
		}
		return collect.get(0);
	}

	private Instance expeditionsAntOnADiscovery(final List<Task> bestPath, Set<List<Task>> alreadyDiscovered) {
		return new Ant()
				.extendTheBestPath(bestPath, instance.getMaintenances(), alreadyDiscovered);
	}

	private Instance expeditionsAntOnAJourney(double independenceRatio) {
		return new Ant() //i don't care about it <3
				.prepareAntPath(independenceRatio, instance.getTasks(), instance.getMaintenances(), pheromoneMatrix);
	}
}
