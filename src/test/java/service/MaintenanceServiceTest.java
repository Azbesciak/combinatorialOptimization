package service;

import model.Maintenance;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class MaintenanceServiceTest {
	@Test
	public void generateMaintenances() throws Exception {
		int totalTime = 1000;
		int totalAmount = 10;
		List<Maintenance> maintenances = MaintenanceService.generateMaintenances(totalTime, totalAmount, 100);
		assertTrue(maintenances.size() == totalAmount);
		int sum = 0;
		for (Maintenance maintenance : maintenances) {
			int duration = maintenance.getDuration();
			sum += duration;
			assertTrue(duration > 0);
			System.out.println(maintenance);
		}
		assertTrue(sum <= totalAmount * Math.max((totalTime * 0.1), 2));
	}

	@Test
	public void tryGenerateWithWrongArg() throws Exception {
		assertThrows(IllegalArgumentException.class,
				() -> MaintenanceService.generateMaintenances(0, 100, 100));
	}

}