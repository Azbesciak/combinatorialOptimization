package service;

import model.Maintenance;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MaintenanceService {
	public static List<Maintenance> generateMaintenances(int totalTime, int totalAmount, int minSpace)
			throws IllegalArgumentException {

		if (totalAmount <= 0 || totalTime <= 0) {
			throw new IllegalArgumentException("Total time and amount have to be positive!");
		}

		int currentEndTime = 0;
		int period = totalTime / totalAmount;
		int maxMaintenanceTime = Math.max((int) (0.05 * totalTime), 2);
		List<Maintenance> maintenances = new ArrayList<>();

		for (int currentAmount = 0; currentAmount < totalAmount; currentAmount++) {
			int maintenanceTime = new Random()
					.nextInt(maxMaintenanceTime) + 1;
			int startTime = new Random().nextInt(period) + currentEndTime + minSpace;
			Maintenance maintenance = new Maintenance(startTime, maintenanceTime);
			currentEndTime = maintenance.getEnd();
			maintenances.add(maintenance);
		}
		return maintenances;
	}
}
