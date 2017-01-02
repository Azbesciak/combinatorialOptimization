package algorithm;

import model.Task;

import java.util.*;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

public class PheromoneMatrix {
	private double evaporationRatio;
	private double[][] pheromonesPath;
	private double[] entryPoints;
	private final double INITIAL_VALUE;
	private final double MAX_VALUE;
	private final double MIN_VALUE;
	private final double INCREMENT_VALUE;
	private int currentBestQuality;


	public PheromoneMatrix(int size, double evaporationRatio) {
		this.evaporationRatio = evaporationRatio;
		pheromonesPath = new double[size][size];
		entryPoints = new double[size];
		MAX_VALUE = size / 10.0;
		INCREMENT_VALUE = size / 100.0;
		INITIAL_VALUE = size / 1000.0;
		MIN_VALUE = size / 10000.0;
		initializeMatrix(INITIAL_VALUE);
	}

	private void initializeMatrix(double initialValue) {
		currentBestQuality = Integer.MAX_VALUE;
		for (int column = 0; column < pheromonesPath.length; column++) {
			entryPoints[column] = initialValue;
			for (int row = 0; row < pheromonesPath.length; row++) {
				if (column == row) {
					pheromonesPath[column][row] = 0;
				} else {
					pheromonesPath[column][row] = initialValue;
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
			for (int column = 0; column < size; column++) {
				if (entryPoints[column] < minValue) {
					entryPoints[column] = MIN_VALUE;
				}
			}
		}
		for (int column = 0; column< size; column++) {
			sorted = DoubleStream.of(pheromonesPath[column]).sorted();
			OptionalDouble rowMin = sorted.limit(size - howMany).max();
			if (rowMin.isPresent()) {
				double rowMinValue = rowMin.getAsDouble();
				for (int row = 0; row < size; row++) {
					if (pheromonesPath[column][row] < rowMinValue) {
						pheromonesPath[column][row] = MIN_VALUE;
					}
				}
			}
		}
	}

	public void resetMatrix() {
		initializeMatrix(INITIAL_VALUE);
	}

	public void evaporateMatrix() {
		for (int column = 0; column < pheromonesPath.length; column++) {
			entryPoints[column] = Math.max(MIN_VALUE, entryPoints[column] * evaporationRatio);
			for (int row = 0; row < pheromonesPath.length; row++) {
				if (column != row) {
					pheromonesPath[column][row] = Math
							.max(MIN_VALUE, pheromonesPath[column][row] * evaporationRatio);
				}
			}
		}
	}

	public void updateMatrix(List<Task> tasks, int quality) {
		int entryId = tasks.get(0).getId();
//		if (quality < currentBestQuality) {
//			if (currentBestQuality < Integer.MAX_VALUE) {
//				multiplyMatrix(quality / (double) currentBestQuality);
//			}
//			currentBestQuality = quality;
//		}
//		double multiplier = currentBestQuality / (double) quality;
		entryPoints[entryId] = Math.min(entryPoints[entryId] + INCREMENT_VALUE, MAX_VALUE);
		for (int number = 0; number < tasks.size() - 1; number++) {
			int currentTaskId = tasks.get(number).getId();
			int nextTaskId = tasks.get(number + 1).getId();
			pheromonesPath[currentTaskId][nextTaskId] = Math
					.min(pheromonesPath[number][nextTaskId] + INCREMENT_VALUE, MAX_VALUE);
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
				", evaporationRatio=" + evaporationRatio +
				'}';
	}
}
