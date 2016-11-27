package service;

import exception.NonNegativeArgException;
import model.Maintenance;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MaintenanceService {
    public static List<Maintenance> generateMaintenances(int totalTime, int totalAmount)
            throws IllegalArgumentException {
        if (totalAmount <= 0 || totalTime <= 0) {
            throw new IllegalArgumentException("Total time and amount have to be positive!");
        }

        int currentEndTime = 0;
        int period = totalTime / totalAmount;
        int minMaintenenceTime = Math.max((int) (0.005 * totalTime), 1);
        int maxMaintenenceTime = Math.max((int) (0.01 * totalTime), 2);
        List<Maintenance> maintenances = new ArrayList<>();

        for (int currentAmount = 0; currentAmount < totalAmount; currentAmount++){
            int maintenanceTime = new Random()
                    .nextInt(maxMaintenenceTime - minMaintenenceTime) + minMaintenenceTime;
            int startTime = new Random().nextInt(period) + currentEndTime;
            Maintenance maintenance = new Maintenance(startTime, maintenanceTime);
            currentEndTime = maintenance.getEndTime();
            maintenances.add(maintenance);
        }
        return maintenances;
    }
}
