package algorithm;

import model.Maintenance;
import model.Task;
import model.wrapper.Instance;
import service.InstanceService;
import service.UtilsService;

import javax.naming.OperationNotSupportedException;
import java.util.*;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Ant {

	public Ant() {
	}

	public Instance extendTheBestPath(final List<Task> theBestPath, List<Maintenance> maintenances,
									   Set<List<Task>> alreadyDiscovered) {
		List<Task> way = UtilsService.deepClone(theBestPath);
		int size = theBestPath.size();
		Random randomGenerator = new Random();
		do {
			Collections.swap(way, randomGenerator.nextInt(size), randomGenerator.nextInt(size));
		} while (alreadyDiscovered.contains(way));
		alreadyDiscovered.add(way);
		return prepareInstance(way, maintenances);
	}

	public Instance prepareAntPath(double independenceRatio, final List<Task> tasks, List<Maintenance> maintenances,
								   PheromoneMatrix matrix) {
		List<Task> way = findAWay(independenceRatio, tasks, matrix);
		return prepareInstance(way, maintenances);
	}

	private Instance prepareInstance(final List<Task> way, List<Maintenance> maintenances) {
		Instance instance = InstanceService.prepareInstance(way, maintenances);
		List<Task> path = instance.getTasks();
		int pathLength = countPathLength(path);
		instance.setQuality(pathLength);
		return instance;
	}

	private List<Task> findAWay(double independenceRatio, final List<Task> tasks, PheromoneMatrix matrix) {

		int chanceForUseMatrix = new Random().nextInt(100);
		List<Task> chosenWay;
		if (chanceForUseMatrix <= independenceRatio) {
			chosenWay = new ArrayList<>(tasks);
			Collections.shuffle(chosenWay);
			return chosenWay;
		} else {
			return createNewPath(tasks, matrix);
		}
	}

	private List<Task> createNewPath(final List<Task> tasks, final PheromoneMatrix matrix) {
		List<Task> newPath = new ArrayList<>(tasks.size());
		Integer[] integers = IntStream.range(0, tasks.size()).boxed().toArray(Integer[]::new);
		Set<Integer> toVisit = new HashSet<>(Arrays.asList(integers));
		Random randomGenerator = new Random();
		double[] entryPoints = matrix.getEntryPoints();

		Double probabilitySum = DoubleStream.of(entryPoints).sum();
		double randomizedEntry = randomGenerator.nextDouble() * probabilitySum;
		double currentScope = 0;
		int currentPosition = -1;
		for (int entryPointIndex = 0; entryPointIndex < entryPoints.length; entryPointIndex++) {
			currentScope += entryPoints[entryPointIndex];
			if (currentScope >= randomizedEntry) {
				toVisit.remove(entryPointIndex);
				currentPosition = entryPointIndex;
				newPath.add(tasks.get(currentPosition));
				break;
			}
		}
		double[][] pheromonesPath = matrix.getPheromonesPath();
		while (!toVisit.isEmpty()) {
			double[] possibleWays = pheromonesPath[currentPosition];
			double pheromonesSum = 0;
			for (int i : toVisit) {
				pheromonesSum += possibleWays[i];
			}
			double nextMovePossibilityScope = randomGenerator.nextDouble() * pheromonesSum;

			currentScope = 0;
			for (int possibleMove : toVisit) {
				currentScope += possibleWays[possibleMove];
				if (currentScope >= nextMovePossibilityScope) {
					toVisit.remove(possibleMove);
					currentPosition = possibleMove;
					newPath.add(tasks.get(currentPosition));
					break;
				}
			}
		}
		return newPath;
	}

	private int countPathLength(List<Task> path) {
		return path
				.stream()
				.mapToInt(t -> t.getFirst().getEnd() + t.getSecond().getEnd())
				.sum();
	}
}
