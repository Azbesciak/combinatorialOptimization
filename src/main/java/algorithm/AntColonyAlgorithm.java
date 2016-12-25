package algorithm;

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

	public Instance run() {
		Instance bestPath = makeIteration(0);
		int initialSchedulingTime = bestPath.getQuality();

		for (int iteration = 1; iteration < iterations; iteration++) {
			Instance result = makeIteration(iteration);
			if (result.getQuality() < bestPath.getQuality()) {
				bestPath = result;
			}
		}
		bestPath.setInitialSchedulingTime(initialSchedulingTime);
		bestPath.getTasks().sort(Comparator.comparing(t -> t.getFirst().getBegin()));
		return bestPath;
	}

	private Instance makeIteration(int iterationNumber) {
		double independenceRatio = 100 * (1 - iterationNumber / (double) iterations);
		Map<Integer, Instance> results = new HashMap<>(antPopulation);
		for (int i = 0; i < antPopulation; i++) {
			Instance result = expeditionsAntOnAJourney(independenceRatio);
			results.put(i, result);
		}
		List<Instance> firstFivePaths = results.values().stream()
				.sorted(Comparator.comparingInt(Instance::getQuality)).limit(solutionPersistenceAmount)
				.collect(Collectors.toList());
		firstFivePaths.forEach(t -> pheromoneMatrix.updateMatrix(t.getTasks()));

		Instance bestResult = firstFivePaths.get(0);
		pheromoneMatrix.evaporateMatrix();

		return bestResult;
	}

	private Instance expeditionsAntOnAJourney(double independenceRatio) {
		return new Ant() //i don't care about it <3
				.prepareAntPath(independenceRatio, instance.getTasks(), instance.getMaintenances(), pheromoneMatrix);
	}
}
