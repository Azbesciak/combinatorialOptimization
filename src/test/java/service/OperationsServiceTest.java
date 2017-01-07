package service;

import enumeration.Machine;
import model.Maintenance;
import model.Operation;
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
		tasks = TaskService.generateTasks(taskAmount, longestTime, 0);
		int totalTime = TaskService.getTotalTasksDuration(tasks);
		maintenances = MaintenanceService.generateMaintenances(totalTime, maintenancesAmount, longestTime, 0);

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
		OperationsService.prepareFirstMachineOperations(tasks, maintenances);

		assertAll("First machine operations",
				() -> assertTrue(tasks.stream().allMatch(t -> t.getFirst().getEnd() != 0 || t.getSecond().getEnd() != 0)),
				() -> assertTrue(tasks.size() == taskAmount)
				);
		for (int i = 1; i < tasks.size(); i++) {
			Operation previous;
			Operation next;
			Task previousTask = tasks.get(i - 1);
			Task nextTask = tasks.get(i);
			previous = Machine.ONE.equals(previousTask.getFirst().getMachine()) ? previousTask.getFirst() : previousTask.getSecond();
			next = Machine.ONE.equals(nextTask.getFirst().getMachine()) ? nextTask.getFirst() : nextTask.getSecond();
			assertTrue(previous.getEnd() <= next.getBegin());
		}

	}

	@Test
	public void prepareSecondMachineOperations() {
		OperationsService.prepareSecondMachineOperations(tasks);
		assertAll("Second machine operations",
				() -> assertTrue(tasks.stream().allMatch(t -> t.getFirst().getEnd() != 0 || t.getSecond().getEnd() != 0)),
				() -> assertTrue(tasks.size() == taskAmount)
		);
		for (int i = 1; i < tasks.size(); i++) {
			Task previousTask = tasks.get(i - 1);
			Task nextTask = tasks.get(i);
			Operation previous = Machine.TWO.equals(previousTask.getFirst().getMachine()) ?
					previousTask.getFirst() : previousTask.getSecond();
			Operation next = Machine.TWO.equals(nextTask.getFirst().getMachine()) ?
					nextTask.getFirst() : nextTask.getSecond();
			Operation machineOneNext = Machine.ONE.equals(nextTask.getFirst().getMachine()) ?
					nextTask.getFirst() : nextTask.getSecond();
			assertTrue(previous.getEnd() <= next.getBegin() && machineOneNext.getEnd() <= next.getBegin());
		}
	}
}
