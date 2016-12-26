package algorithm;

import model.Task;
import model.wrapper.Instance;

import java.util.*;
import java.util.stream.Collectors;

public class AntColonyAlgorithm {

	private final PheromoneMatrix pheromoneMatrix;
	private final int iterations;
	private final int antPopulation;
	private final Instance instance;
	private final int solutionPersistenceAmount;

	public AntColonyAlgorithm(final int iterations, final int antPopulation, final double evaporationRatio,
							  final Instance instance, final int solutionPersistenceAmount) {
		this.iterations = iterations;
		this.antPopulation = antPopulation;
		this.instance = instance;
		this.solutionPersistenceAmount = solutionPersistenceAmount;
		this.pheromoneMatrix = new PheromoneMatrix(instance.getTasks().size(), evaporationRatio);
	}

	public Instance run(boolean isSmart) {
		Instance bestPath = makeSimpleIteration(0);
		int initialSchedulingTime = bestPath.getQuality();
		if (isSmart) {
			for (int iteration = 1; iteration < iterations; iteration++) {
				Instance result = makeSmartIteration(iteration, iterations, bestPath);
				if (result.getQuality() < bestPath.getQuality()) {
					bestPath = result;
				}
			}
		} else {
			for (int iteration = 1; iteration < iterations; iteration++) {
				Instance result = makeSimpleIteration(iteration);
				if (result.getQuality() < bestPath.getQuality()) {
					bestPath = result;
				}
			}
		}

		bestPath.setInitialSchedulingTime(initialSchedulingTime);
		Collections.sort(bestPath.getTasks());
		return bestPath;
	}

	private Instance makeSmartIteration(int iterationNumber, int totalIterations, Instance bestResult) {
		int multiplicationOfFifty = (iterationNumber + 1) % 100;
		double independenceRatio = getSmartIndependenceRatio(multiplicationOfFifty);
		boolean isMultiplicationOfFifty = multiplicationOfFifty == 0;
		Map<Integer, Instance> results = searchForSolution(independenceRatio);

		boolean shouldResetMatrix = isMultiplicationOfFifty && iterationNumber < totalIterations - 1;
		Instance instance = persistBestOnesAndReturnFirst(results, shouldResetMatrix);
		if (shouldResetMatrix && instance.getQuality() > bestResult.getQuality()) {
			pheromoneMatrix.updateMatrix(bestResult.getTasks(), solutionPersistenceAmount);
		}
		return instance;
	}

	private double getSmartIndependenceRatio(int condition) {
		if (condition < 25) {
			return 100;
		} else if (condition < 70) {
			return 20;
		} else {
			return 0;
		}
	}

	private Instance makeSimpleIteration(int iterationNumber) {
		double independenceRatio = 100 * (1 - iterationNumber / (double) iterations);
		Map<Integer, Instance> results = searchForSolution(independenceRatio);
		return persistBestOnesAndReturnFirst(results, false);
	}

	private Map<Integer, Instance> searchForSolution(double independenceRatio) {
		Map<Integer, Instance> results = new HashMap<>(antPopulation);
		for (int i = 0; i < antPopulation; i++) {
			Instance result = expeditionsAntOnAJourney(independenceRatio);
			results.put(i, result);
		}
		return results;
	}

	private Instance persistBestOnesAndReturnFirst(Map<Integer, Instance> results, boolean shouldResetMatrix) {
		List<Instance> toPersist = results.values().stream()
				.sorted(Comparator.comparingInt(Instance::getQuality)).limit(solutionPersistenceAmount)
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

	private Instance expeditionsAntOnAJourney(double independenceRatio) {
		return new Ant() //i don't care about it <3
				.prepareAntPath(independenceRatio, instance.getTasks(), instance.getMaintenances(), pheromoneMatrix);
	}
}
