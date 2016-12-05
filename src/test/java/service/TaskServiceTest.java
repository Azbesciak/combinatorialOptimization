package service;

import exception.NonNegativeArgException;
import model.Task;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class TaskServiceTest {
	@Test
	public void generateTasks() throws Exception {

		int longestTime = 15;
		int amount = 100;
		List<Task> tasks = TaskService.generateTasks(amount, longestTime);
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
	public void generateTasksNegativeLongTime() throws Exception {
		int longestTime = 1;
		int amount = 0;
		assertThrows(NonNegativeArgException.class, () -> TaskService.generateTasks(amount, longestTime));
	}

	@Test
	public void generateTasksNegativeAmount() throws Exception {
		int longestTime = 0;
		int amount = 1;
		assertThrows(NonNegativeArgException.class, () -> TaskService.generateTasks(amount, longestTime));
	}


}