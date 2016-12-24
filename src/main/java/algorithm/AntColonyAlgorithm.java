package algorithm;

import model.Task;
import model.wrapper.Instance;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AntColonyAlgorithm {

	private final PheromoneMatrix pheromoneMatrix;
	private final int iterations;
	private final int antPopulation;
	private final Instance instance;
	private final int solutionPersistenceAmount;
	private final List<Instance> bestPaths;

	public AntColonyAlgorithm(final int iterations, final int antPopulation, final double evaporationRatio,
							  final Instance instance, final int solutionPersistenceAmount) {
		this.iterations = iterations;
		this.antPopulation = antPopulation;
		this.instance = instance;
		this.solutionPersistenceAmount = solutionPersistenceAmount;
		this.bestPaths = new ArrayList<>(iterations);
		this.pheromoneMatrix = new PheromoneMatrix(instance.getTasks().size(), evaporationRatio);
	}

	public Instance run() {
		makeIteration(0);
		int initialSchedulingTime = bestPaths.get(0).getInitialSchedulingTime();
		for (int iteration = 1; iteration < iterations; iteration++) {
			makeIteration(iteration);
		}
		bestPaths.sort(Comparator.comparingInt(Instance::getQuality));
		Instance instance = bestPaths.get(0);
		instance.setInitialSchedulingTime(initialSchedulingTime);
		return instance;
	}

	private void makeIteration(int iterationNumber) {

		double independenceRatio = 100 - (iterationNumber / iterations);
		ConcurrentHashMap<Integer, Instance> results = new ConcurrentHashMap<>(antPopulation);
		for (int i = 0; i < antPopulation; i++) {
			Ant ant = new Ant();
			Instance instance = ant
					.prepareAntPath(independenceRatio, this.instance.getTasks(), this.instance.getMaintenances(),
							pheromoneMatrix);
			results.put(i, instance);
		}

		List<Instance> firstFivePaths = results.values().stream()
				.sorted(Comparator.comparingInt(Instance::getCurrentSchedulingTime)).limit(solutionPersistenceAmount)
				.collect(
						Collectors.toList());
		firstFivePaths.forEach(t -> pheromoneMatrix.updateMatrix(t.getTasks()));
		bestPaths.add(firstFivePaths.get(0));
		pheromoneMatrix.evaporateMatrix();
	}
}
