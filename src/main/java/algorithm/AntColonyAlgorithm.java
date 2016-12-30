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
	private final static int RANDOM_SOLUTIONS_EDGE = 25;
	private final static int AMNESIA_REQUIREMENT = 30000;
	private final static String CHART_FOLDER = "chartData";

	private final Logger logger;
	private final PheromoneMatrix pheromoneMatrix;
	private final int iterations;
	private final int antPopulation;
	private final Instance instance;
	private final int solutionPersistenceAmount;
	private final int smallIterationBorder = 100;

	public AntColonyAlgorithm(final int iterations, final int antPopulation, final double evaporationRatio,
							  final Instance instance, final int solutionPersistenceAmount) {
		this.iterations = iterations;
		this.antPopulation = antPopulation;
		this.instance = instance;
		this.solutionPersistenceAmount = solutionPersistenceAmount;
		this.pheromoneMatrix = new PheromoneMatrix(instance.getTasks().size(), evaporationRatio);
		logger = Logger.getLogger(AntColonyAlgorithm.class.getName());
		logger.setUseParentHandlers(false);

		System.setProperty("java.util.logging.SimpleFormatter.format","%5$s%6$s%n");
		try {
			String savePathForNewFile = UtilsService
					.getSavePathForNewFile(CHART_FOLDER, "It" + iterations + "An" + antPopulation);
			FileHandler fh = new FileHandler(savePathForNewFile + ".log");
			logger.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
	}

	public Instance run(boolean isSmart, long endTime) {
		final Set<List<Task>> alreadyDiscovered = new HashSet<>();
		Instance currentPath = makeSimpleIteration(0);
		Instance bestPath = null;
		int localOptimumDefender = 0;
		int initialSchedulingTime = currentPath.getQuality();
		if (isSmart) {
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
				Instance result = makeSmartIteration(iteration, iterations, currentPath, alreadyDiscovered);
				if (result.getQuality() / (double) currentPath.getQuality() >= 0.9995) {
					if (localOptimumDefender >= smallIterationBorder) {
						if ((bestPath != null && bestPath.getQuality() > currentPath
								.getQuality()) || bestPath == null) {
							bestPath = currentPath;
						}
						currentPath = makeSimpleIteration(0);
						iteration++;
						localOptimumDefender = 0;
						pheromoneMatrix.resetMatrix();
//						bestPaths.forEach(t -> pheromoneMatrix.updateMatrix(t.getTasks(), 1));
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
		} else {
			for (int iteration = 1; iteration < iterations; iteration++) {
				Instance result = makeSimpleIteration(iteration);
				if (result.getQuality() < currentPath.getQuality()) {
					currentPath = result;
				}
			}
		}

		currentPath.setInitialSchedulingTime(initialSchedulingTime);
		Collections.sort(currentPath.getTasks());
		return currentPath;
	}

	private Instance makeSmartIteration(int iterationNumber, int totalIterations, Instance bestResult,
										Set<List<Task>> alreadyDiscovered) {
		int smallIteration = (iterationNumber + 1) % smallIterationBorder;
		double independenceRatio = getSmartIndependenceRatio(smallIteration);
		boolean smallIterationReset = smallIteration == 0;
		Set<Instance> results;
		if (iterationNumber >= RANDOM_SOLUTIONS_EDGE && iterationNumber % 10 == 0) {
			if (AMNESIA_REQUIREMENT <= alreadyDiscovered.size()) {
				alreadyDiscovered.clear();
			}
			results = extendTheBestSolution(bestResult, alreadyDiscovered);
		} else {
			results = extendTheBestSolution(independenceRatio);
		}
		boolean shouldResetMatrix = smallIterationReset && iterationNumber < totalIterations - 1;
		Instance instance = persistBestOnesAndReturnFirst(results, shouldResetMatrix);
		if (shouldResetMatrix && instance.getQuality() > bestResult.getQuality()) {
			pheromoneMatrix.updateMatrix(bestResult.getTasks(), solutionPersistenceAmount);
		}
		return instance;
	}

	private double getSmartIndependenceRatio(int condition) {
		if (condition < RANDOM_SOLUTIONS_EDGE) {
			return smallIterationBorder;
		} else if (condition < 70) {
			return 15;
		} else {
			return 0;
		}
	}

	private Instance makeSimpleIteration(int iterationNumber) {
		double independenceRatio = smallIterationBorder * (1 - iterationNumber / (double) iterations);
		Set<Instance> results = extendTheBestSolution(independenceRatio);

		return persistBestOnesAndReturnFirst(results, false);
	}

	private Set<Instance> extendTheBestSolution(double independenceRatio) {
		Set<Instance> results = new TreeSet<>();
		for (int i = 0; i < antPopulation; i++) {
			Instance result = expeditionsAntOnAJourney(independenceRatio);
			results.add(result);
		}
		return results;
	}

	private Set<Instance> extendTheBestSolution(Instance bestResult, Set<List<Task>> alreadyDiscovered) {
		Set<Instance> results = new TreeSet<>();
		alreadyDiscovered.add(bestResult.getTasks());

		for (int i = 0; i < antPopulation; i++) {
			Instance result = expeditionsAntOnADiscovery(bestResult.getTasks(), alreadyDiscovered);
			results.add(result);
			if (result.getQuality() < bestResult.getQuality()) {
				bestResult = result;
			}
		}
		return results;
	}

	private Instance persistBestOnesAndReturnFirst(Set<Instance> results, boolean shouldResetMatrix) {
		List<Instance> toPersist = results.stream()
				.limit(solutionPersistenceAmount)
				.collect(Collectors.toList());
		if (shouldResetMatrix) {
			pheromoneMatrix.resetMatrix();
		}
		for (int index = 0; index < solutionPersistenceAmount; index++) {
			List<Task> tasks = toPersist.get(index).getTasks();
			pheromoneMatrix.updateMatrix(tasks, solutionPersistenceAmount - index);
		}

		Instance bestResult = toPersist.get(0);
		pheromoneMatrix.evaporateMatrix();

		return bestResult;
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
