package service;

import enumeration.Machine;
import model.Maintenance;
import model.Task;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class OperationsServiceTest {

	private List<Task> tasks;
	private List<Maintenance> maintenances;
	private int longestTime;
	private int taskAmount;
	private int maintenancesAmount;

	@BeforeEach
	public void prepareTasks() throws Exception {
		longestTime = 100;
		taskAmount = 80;
		maintenancesAmount = taskAmount / 4;
		tasks = TaskService.generateTasks(taskAmount, longestTime);
		int totalTime = TaskService.getTotalTasksDuration(tasks);
		maintenances = MaintenanceService.generateMaintenances(totalTime, maintenancesAmount);
	}

	@AfterEach
	public void cleanTasks() {
		tasks = null;
		maintenances = null;
		longestTime = 0;
		taskAmount = 0;
		maintenancesAmount = 0;
	}

	@Test
	public void assignOperationsToMachines() throws Exception {
		OperationsService.assignOperationsToMachines(tasks);
		assertAll("operations assignment",
				() -> assertTrue(tasks.parallelStream()
						.allMatch(t -> t.getFirst().getMachine() != null && t.getSecond().getMachine() != null)),
				() -> assertTrue(tasks.parallelStream()
						.allMatch(t -> (t.getFirst().getMachine() == Machine.ONE &&
										t.getSecond().getMachine() == Machine.TWO) ||
										(t.getFirst().getMachine() == Machine.TWO &&
										t.getSecond().getMachine() == Machine.ONE)
						)));
	}

	@Test
	public void prepareMachineFirstOperations() throws Exception {
		OperationsService.assignOperationsToMachines(tasks);
		OperationsService.prepareFirstMachineOperations(tasks, maintenances);
		assertTrue(tasks.size() == taskAmount);
		int previousEnd = -1;
//		Iterator<Maintenance> iterator = maintenances.iterator();
//		Maintenance maintenance = null;
//		if (iterator.hasNext()) {
//			maintenance = iterator.next();
//		}
		assertAll("First machine operations",
				() -> assertTrue(tasks.stream().allMatch(t -> t.getFirst().getEnd() != 0 || t.getSecond().getEnd() != 0))
//				() -> assertTrue(tasks.stream().allMatch((a, b) -> a.getFirst().getEnd() < b.getFirst().getBegin()))
		);

	}

	@Test
	public void prepareSecondMachineOperations() {
		OperationsService.assignOperationsToMachines(tasks);
		OperationsService.prepareSecondMachineOperations(tasks);
	}
}
