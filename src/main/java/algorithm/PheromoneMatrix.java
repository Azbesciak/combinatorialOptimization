package algorithm;

import model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PheromoneMatrix {
	private List<Map<Integer, Double>> pheromonesPath;
	private List<Double> entryPoints;
	private double evaporationRatio;

	public PheromoneMatrix(int size, double evaporationRatio) {
		this(size, evaporationRatio, 1.0);
	}

	private PheromoneMatrix(int size, double evaporationRatio, double initialValue) {
		this.evaporationRatio = evaporationRatio;
		this.pheromonesPath = new ArrayList<>(size);
		this.entryPoints = new ArrayList<>(size);
		initializeMatrix(size, initialValue);
	}

	public static PheromoneMatrix prepareTestMatrixWithZeros(int size, double evaporationRatio) {
		return new PheromoneMatrix(size, evaporationRatio, 0.0);
	}

	private void initializeMatrix(int size, double initialValue) {
		for (int column = 0; column < size; column++) {
			entryPoints.add(initialValue);
			Map<Integer, Double> pheromonesOnPath = new HashMap<>();
			for (int row = 0; row < size; row++) {
				pheromonesOnPath.put(row, initialValue);
			}
			pheromonesPath.add(pheromonesOnPath);
		}
	}

	public void evaporateMatrix() {
		for (int columnNumber = 0; columnNumber < pheromonesPath.size(); columnNumber++) {
			Map<Integer, Double> column = pheromonesPath.get(columnNumber);
			for (Integer rowNumber : column.keySet()) {
				Double currentValue = column.get(rowNumber);
				column.replace(rowNumber, currentValue * evaporationRatio);
			}

			Double aDouble = entryPoints.get(columnNumber);
			entryPoints.set(columnNumber, aDouble * evaporationRatio);
		}
	}

	public void updateMatrix(List<Task> tasks) {
		int entryId = tasks.get(0).getId();
		Double currentEntryPointPheromoneValue = entryPoints.get(entryId);
		entryPoints.set(entryId, currentEntryPointPheromoneValue + 1);
		for (int number = 0; number < tasks.size() - 1; number++) {
			Map<Integer, Double> currentPosition = pheromonesPath.get(number);
			int nextTaskId = tasks.get(number + 1).getId();
			Double nextIdPheromoneValue = currentPosition.get(nextTaskId);
			currentPosition.replace(nextTaskId, nextIdPheromoneValue + 1);
		}
	}

	public List<Map<Integer, Double>> getPheromonesPath() {
		return pheromonesPath;
	}

	public void setPheromonesPath(List<Map<Integer, Double>> pheromonesPath) {
		this.pheromonesPath = pheromonesPath;
	}

	public List<Double> getEntryPoints() {
		return entryPoints;
	}

	public void setEntryPoints(List<Double> entryPoints) {
		this.entryPoints = entryPoints;
	}

	@Override
	public String toString() {
		return "PheromoneMatrix{" +
				"pheromonesPath=" + pheromonesPath +
				", entryPoints=" + entryPoints +
				", evaporationRatio=" + evaporationRatio +
				'}';
	}
}
