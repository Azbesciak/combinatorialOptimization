package algorithm;

import model.Task;
import model.wrapper.Instance;
import service.UtilsService;

import java.io.IOException;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class AntColonyAlgorithm {
	private final static String CHART_FOLDER = "chartData";
	private final static double RANDOM_SOLUTIONS_EDGE = 0.25;
	private final static int AMNESIA_REQUIREMENT = 30000;

	private static final double SEMI_MATRIX_SOLUTION_EDGE = 0.7;
	private static final int SEMI_MATRIX_SOLUTION_RANDOM_SCALE = 15;
	private final static int SMALL_ITERATION_BORDER = 100;
	private final static int STAGNATION_BORDER = 100;

	private final Logger logger;
	private final PheromoneMatrix pheromoneMatrix;
	private final int iterations;
	private final long endTime;
	private final int antPopulation;
	private final Instance instance;
	private int pathExplorationReq = 10;

	public AntColonyAlgorithm(final int iterations, long endTime, final int antPopulation,
							  final double evaporationRatio, final Instance instance, String prefix, int pathExplorationReq) {
		this.iterations = iterations;
		this.endTime = endTime;
		this.antPopulation = antPopulation;
		this.instance = instance;
		this.pheromoneMatrix = new PheromoneMatrix(instance.getTasks().size(), evaporationRatio);
		this.pathExplorationReq = pathExplorationReq;
		logger = Logger.getLogger(this.toString());
		logger.setUseParentHandlers(false);

		System.setProperty("java.util.logging.SimpleFormatter.format", "%5$s%6$s%n");
		try {
			String savePathForNewFile = UtilsService
					.getSavePathForNewFile(CHART_FOLDER,
							prefix + "It" + iterations + "An" + antPopulation + "Ev" + evaporationRatio);
			FileHandler fh = new FileHandler(savePathForNewFile + ".log");
			logger.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
	}

	public Instance run() {
		final Set<List<Task>> alreadyDiscovered = new HashSet<>();
		Instance currentPath = randomIteration();
		Instance bestPath = null;
		int localOptimumDefender = 0;
		int initialSchedulingTime = currentPath.getQuality();
		int resetMoment = 0;
		long start = System.currentTimeMillis();
		for (int iteration = 1; iteration < iterations || System.currentTimeMillis() < endTime; iteration++) {
			if (iteration % 10 == 0) {
				logger.info(iteration + " " + currentPath.getQuality());
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
			Instance result = makeSmartIteration(iteration , resetMoment, iterations, currentPath, alreadyDiscovered, endTime != 0);
			if (result.getQuality() / (double) currentPath.getQuality() >= 0.9995) {
				if (localOptimumDefender >= STAGNATION_BORDER) {
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
		double independenceRatio = getIndependenceRatio(smallIteration, iterationNumber < SMALL_ITERATION_BORDER);
		boolean smallIterationReset = smallIteration == 0;
		Instance result;
		if (iterationNumber % pathExplorationReq == 0) {
			if (AMNESIA_REQUIREMENT <= alreadyDiscovered.size()) {
				alreadyDiscovered.clear();
			}
			result = extendTheBestSolution(bestResult, alreadyDiscovered);
		} else {

			result = goForResearch(independenceRatio);
		}
		boolean shouldResetMatrix = smallIterationReset && (iterationNumber < totalIterations - 1 || isTimeLimited);
		Instance instance = persistFirstAndGet(result);
		if (shouldResetMatrix && instance.getQuality() > bestResult.getQuality()) {

			pheromoneMatrix.updateMatrix(bestResult.getTasks(), bestResult.getQuality());
		}
		return instance;
	}


	private double getIndependenceRatio(int condition, boolean isFirstCycle) {
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
		Instance result = goForResearch(independenceRatio);

		return persistFirstAndGet(result);
	}

	private Instance goForResearch(double independenceRatio) {
		Instance bestResult = null;
		for (int i = 0; i < antPopulation; i++) {
			Instance result = expeditionsAntOnAJourney(independenceRatio);
			if (bestResult == null || bestResult.getQuality() > result.getQuality()) {
				bestResult = result;
			}
		}
		return bestResult;
	}

	private Instance extendTheBestSolution(Instance bestResult, Set<List<Task>> alreadyDiscovered) {
		alreadyDiscovered.add(bestResult.getTasks());
		for (int i = 0; i < antPopulation; i++) {
			Instance result = expeditionsAntOnADiscovery(bestResult.getTasks(), alreadyDiscovered);
			if (result.getQuality() < bestResult.getQuality()) {
				bestResult = result;
			}
		}
		return bestResult;
	}

	private Instance persistFirstAndGet(Instance result) {
			pheromoneMatrix.evaporateMatrix();
			pheromoneMatrix.updateMatrix(result.getTasks(), result.getQuality());
			return result;
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
