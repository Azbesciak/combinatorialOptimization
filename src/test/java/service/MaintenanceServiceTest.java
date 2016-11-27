package service;

import exception.NonNegativeArgException;
import model.Maintenance;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class MaintenanceServiceTest {
    @Test
    public void generateMaintenances() throws Exception {
        int totalTime = 1000;
        int totalAmount = 10;
        List<Maintenance> maintenances = MaintenanceService.generateMaintenances(totalTime, totalAmount);
        assertTrue(maintenances.size() == totalAmount);
        int sum = 0;
        for (Maintenance maintenance : maintenances) {
            int duration = maintenance.getDuration();
            sum += duration;
            assertTrue(duration > 0);
        }
        assertTrue(sum <= totalAmount * Math.max((totalTime * 0.01), 2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void tryGenerateWithWrongArg() throws Exception {
        MaintenanceService.generateMaintenances(0, 100);
        fail("Exception should occur");
    }

}