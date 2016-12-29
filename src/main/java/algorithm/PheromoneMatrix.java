package algorithm;

import model.Task;

import java.util.*;

public class PheromoneMatrix {
	private double evaporationRatio;
	private double[][] pheromonesPath;
	private double[] entryPoints;
	private final double INITIAL_VALUE;
	private final double MAX_VALUE;
	private final double MIN_VALUE;
	private final double INCREMENT_VALUE;


//	public PheromoneMatrix(int size, double evaporationRatio) {
//		this(size, evaporationRatio, INITIAL_VALUE);
//	}

	public PheromoneMatrix(int size, double evaporationRatio) {
		this.evaporationRatio = evaporationRatio;
		this.pheromonesPath = new double[size][size];
		this.entryPoints = new double[size];
		MAX_VALUE = size / 10.0;
		INCREMENT_VALUE = size / 100.0;
		INITIAL_VALUE = size / 1000.0;
		MIN_VALUE = size / 2000.0;
		initializeMatrix(INITIAL_VALUE);
	}

//	public static PheromoneMatrix prepareTestMatrixWithZeros(int size, double evaporationRatio) {
//		return new PheromoneMatrix(size, evaporationRatio, 0.0);
//	}

	private void initializeMatrix(double initialValue) {
		for (int column = 0; column < pheromonesPath.length; column++) {
			entryPoints[column] = initialValue;
			for (int row = 0; row < pheromonesPath.length; row++) {
				pheromonesPath[column][row] = initialValue;
			}
		}
	}

	public void resetMatrix() {
		initializeMatrix(INITIAL_VALUE);
	}

	public void evaporateMatrix() {
		for (int columnNumber = 0; columnNumber < pheromonesPath.length; columnNumber++) {
			entryPoints[columnNumber] = Math.max(MIN_VALUE, entryPoints[columnNumber] * evaporationRatio);
			for (int rowNumber = 0; rowNumber < pheromonesPath.length; rowNumber++) {
				pheromonesPath[columnNumber][rowNumber] = Math
						.max(MIN_VALUE, pheromonesPath[columnNumber][rowNumber] * evaporationRatio);
			}
		}
	}

	public void updateMatrix(List<Task> tasks, int multiplier) {
		int entryId = tasks.get(0).getId();
		entryPoints[entryId] = Math.min(multiplier * entryPoints[entryId] * INCREMENT_VALUE, MAX_VALUE);
		for (int number = 0; number < tasks.size() - 1; number++) {
			int nextTaskId = tasks.get(number + 1).getId();
			pheromonesPath[number][nextTaskId] = Math
					.min(multiplier * pheromonesPath[number][nextTaskId] * INCREMENT_VALUE, MAX_VALUE);
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
