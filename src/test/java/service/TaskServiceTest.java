package service;

import enumeration.Machine;
import exception.NonNegativeArgException;
import model.Maintenance;
import model.Operation;
import model.Task;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class TaskServiceTest {
	@Test
	public void generateTasks() throws Exception {

		int longestTime = 15;
		int amount = 100;
		List<Task> tasks = TaskService.generateTasks(amount, longestTime, 0);
		assertEquals(amount, tasks.size());
		for (Task task : tasks) {

			int firstDuration = task.getFirst().getDuration();
			assertTrue(firstDuration < longestTime);
			assertTrue(firstDuration > 0);

			int secondDuration = task.getSecond().getDuration();
			assertTrue(secondDuration > 0);
			assertTrue(secondDuration < longestTime);
		}
	}

	@Test
	public void readyTimeTest() throws Exception {
		int longestTime = 15;
		int amount = 100;
		List<Task> tasks = TaskService.generateTasks(amount, longestTime, 0);
		int totalTasksDuration = TaskService.getTotalTasksDuration(tasks);
		for (Task task : tasks) {
			assertTrue(task.getFirst().getReadyTime() < 0.5 * totalTasksDuration);
			assertTrue(task.getSecond().getReadyTime() < 0.5 * totalTasksDuration);
		}
	}

	@Test
	public void generateTasksNegativeLongTime() throws Exception {
		int longestTime = 1;
		int amount = 0;
		assertThrows(NonNegativeArgException.class, () -> TaskService.generateTasks(amount, longestTime, 0));
	}

	@Test
	public void generateTasksNegativeAmount() throws Exception {
		int longestTime = 0;
		int amount = 1;
		assertThrows(NonNegativeArgException.class, () -> TaskService.generateTasks(amount, longestTime, 0));
	}

	@Test
	public void randomGeneratorTest() throws  Exception {
		int longestTime = 15;
		int amount = 100;
		List<Task> tasks = TaskService.generateTasks(amount, longestTime, 0);
		int totalTasksDuration = TaskService.getTotalTasksDuration(tasks);
		List<Maintenance> maintenances = MaintenanceService.
				generateMaintenances(totalTasksDuration, amount / 4, longestTime, 0);
		List<Task> randoms = TaskService.randomGenerator(maintenances, tasks);
		assertAll("randomGeneratorTest",
				() -> assertTrue(randoms.stream().allMatch(t -> t.getId() < amount)),
				() -> assertTrue(randoms.parallelStream().allMatch(t -> t.getFirst().getEnd() > 0 && t.getSecond().getEnd() > 0)),
				() -> assertTrue(randoms.parallelStream().allMatch(this::randomGeneratorTestOperationsSequence)));
	}

	private boolean randomGeneratorTestOperationsSequence(Task task) {
		Operation first = task.getFirst();
		Operation second = task.getSecond();
		if (Machine.ONE.equals(first.getMachine())) {
			return first.getEnd() <= second.getBegin();
		} else
			return second.getEnd() <= first.getBegin();
	}


}