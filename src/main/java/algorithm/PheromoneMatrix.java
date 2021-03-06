package algorithm;

import model.Task;

import java.util.*;
import java.util.stream.DoubleStream;

public class PheromoneMatrix {
	private double persistenceRate;
	private double[][] pheromonesPath;
	private double[] entryPoints;
	private final double INITIAL_VALUE;
	private final double MAX_VALUE;
	private final double MIN_VALUE;
	private final double INCREMENT_VALUE;
	private int firstSolutionQuality;


	public PheromoneMatrix(int size, double evaporationRate) {
		this.persistenceRate = 1 - evaporationRate;
		pheromonesPath = new double[size][size];
		entryPoints = new double[size];
		MAX_VALUE = size / 10.0;
		INCREMENT_VALUE = size / 100.0;
		INITIAL_VALUE = size / 1000.0;
		MIN_VALUE = size / 10000.0;
		initializeMatrix(INITIAL_VALUE);
	}

	private void initializeMatrix(double initialValue) {
		firstSolutionQuality = Integer.MAX_VALUE;
		for (int row = 0; row < pheromonesPath.length; row++) {
			entryPoints[row] = initialValue;
			for (int column = 0; column < pheromonesPath.length; column++) {
				if (row == column) {
					pheromonesPath[row][column] = 0;
				} else {
					pheromonesPath[row][column] = initialValue;
				}
			}
		}
	}

	public void saveBestOnes(int howMany) {
		int size = entryPoints.length;
		DoubleStream sorted = DoubleStream.of(entryPoints).sorted();
		OptionalDouble entryMin = sorted.limit(size - howMany).max();
		if (entryMin.isPresent()) {
			double minValue = entryMin.getAsDouble();
			for (int row = 0; row < size; row++) {
				if (entryPoints[row] < minValue) {
					entryPoints[row] = MIN_VALUE;
				}
			}
		}
		for (int row = 0; row< size; row++) {
			sorted = DoubleStream.of(pheromonesPath[row]).sorted();
			OptionalDouble rowMin = sorted.limit(size - howMany).max();
			if (rowMin.isPresent()) {
				double rowMinValue = rowMin.getAsDouble();
				for (int column = 0; column < size; column++) {
					if (pheromonesPath[row][column] < rowMinValue) {
						pheromonesPath[row][column] = MIN_VALUE;
					}
				}
			}
		}
	}

	public void resetMatrix() {
		initializeMatrix(INITIAL_VALUE);
	}

	public void evaporateMatrix() {
		for (int row = 0; row < pheromonesPath.length; row++) {
			entryPoints[row] = Math.max(MIN_VALUE, entryPoints[row] * persistenceRate);
			for (int column = 0; column < pheromonesPath.length; column++) {
				if (row != column) {
					pheromonesPath[row][column] = Math
							.max(MIN_VALUE, pheromonesPath[row][column] * persistenceRate);
				}
			}
		}
	}

	public void updateMatrix(List<Task> tasks, int quality) {
		int entryId = tasks.get(0).getId();
		if (firstSolutionQuality == Integer.MAX_VALUE) {
			firstSolutionQuality = quality;
		}
		double pheromone = INCREMENT_VALUE * firstSolutionQuality / (double) quality;
		entryPoints[entryId] = Math.min(entryPoints[entryId] + pheromone, MAX_VALUE);
		for (int number = 0; number < tasks.size() - 1; number++) {
			int currentTaskId = tasks.get(number).getId();
			int nextTaskId = tasks.get(number + 1).getId();
			pheromonesPath[currentTaskId][nextTaskId] = Math
					.min(pheromonesPath[number][nextTaskId] + pheromone, MAX_VALUE);
		}
	}

	private void multiplyMatrix(double multiplier) {
		for (int column = 0; column < pheromonesPath.length; column++) {
			entryPoints[column] *= multiplier;
			for (int row = 0; row < pheromonesPath.length; row++) {
				pheromonesPath[column][row] *= multiplier;
			}
		}
	}

	public double[][] getPheromonesPath() {
		return pheromonesPath;
	}

	public double[] getEntryPoints() {
		return entryPoints;
	}

	@Override
	public String toString() {
		return "PheromoneMatrix{" +
				"pheromonesPath=" + Arrays.deepToString(pheromonesPath) +
				", entryPoints=" + Arrays.toString(entryPoints) +
				", persistenceRate=" + persistenceRate +
				'}';
	}
}
