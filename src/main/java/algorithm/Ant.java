package algorithm;

import model.Maintenance;
import model.Task;
import model.wrapper.Instance;
import service.InstanceService;

import javax.naming.OperationNotSupportedException;
import java.util.*;

public class Ant {
	private List<Task> path;
	private int pathLength;

	public Ant() {
		this.path = new ArrayList<>();
		this.pathLength = -1;
	}

	public int getPathLength() throws OperationNotSupportedException {
		if (this.pathLength == -1) {
			throw new OperationNotSupportedException("PATH NOT CREATED");
		}
		return this.pathLength;
	}

	public Instance prepareAntPath(double independenceRatio, final List<Task> tasks, List<Maintenance> maintenances,
								   PheromoneMatrix matrix) {
		List<Task> way = findAWay(independenceRatio, tasks, matrix);
		Instance instance = InstanceService.prepareInstance(way, maintenances);
		this.path = instance.getTasks();
		this.pathLength = countPathLength();
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
		Set<Integer> visited = new HashSet<>();
		Random randomGenerator = new Random();
		List<Double> entryPoints = matrix.getEntryPoints();
		Double probabilitySum = entryPoints.stream().reduce(0.0, Double::sum);
		double randomizedEntry = randomGenerator.nextDouble() * probabilitySum;
		double currentScope = 0;
		int currentPosition = -1;
		for (int entryPointIndex = 0; entryPointIndex < entryPoints.size(); entryPointIndex++) {
			currentScope += entryPoints.get(entryPointIndex);
			if (currentScope >= randomizedEntry) {
				visited.add(entryPointIndex);
				currentPosition = entryPointIndex;
				newPath.add(tasks.get(currentPosition));
				break;
			}
		}
		List<Map<Integer, Double>> pheromonesPath = matrix.getPheromonesPath();
		while (visited.size() < tasks.size()) {
			Map<Integer, Double> originalRow = pheromonesPath.get(currentPosition);
			HashMap<Integer, Double> possibleWays = new HashMap<>(originalRow);
			possibleWays.keySet().removeAll(visited);

			double pheromonesSum = possibleWays.values().stream().mapToDouble(Double::doubleValue).sum();
			double nextMovePossibilityScope = randomGenerator.nextDouble() * pheromonesSum;

			currentScope = 0;
			for (Integer possibleMove : possibleWays.keySet()) {
				currentScope += possibleWays.get(possibleMove);
				if (currentScope >= nextMovePossibilityScope) {
					visited.add(possibleMove);
					currentPosition = possibleMove;
					newPath.add(tasks.get(currentPosition));
					break;
				}
			}
		}
		return newPath;
	}

	private int countPathLength() {
		return path
				.stream()
				.mapToInt(t -> t.getFirst().getEnd() + t.getSecond().getEnd())
				.sum();
	}

	public List<Task> getPath() {
		return path;
	}

	@Override
	public String toString() {
		return "Ant{" +
				"path=" + path +
				", pathLength=" + pathLength +
				'}';
	}
}
