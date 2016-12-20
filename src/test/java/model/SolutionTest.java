package model;

import enumeration.Machine;
import model.wrapper.Instance;
import model.wrapper.TimeLineEvent;
import org.junit.jupiter.api.Test;

import service.SolutionService;
import service.TaskService;
import startup.TestStartup;

import java.util.Iterator;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class SolutionTest {

	@Test
	public void prepareTimeLineForMachineOneTest() throws Exception {
		Instance instance = TestStartup.prepareTasks();
		List<Task> tasks = TaskService.randomGenerator(instance.getMaintenances(), instance.getTasks());
		instance.setTasks(tasks);
		Iterator<Maintenance> iterator = instance.getMaintenances().iterator();
		List<TimeLineEvent> events = SolutionService.prepareTimeLineForMachine(Machine.ONE, iterator, instance);
		events.forEach(System.out::println);
		for (int i = 1; i < events.size(); i++) {
			TimeLineEvent recent = events.get(i - 1);
			TimeLineEvent current = events.get(i);
			assertTrue(0 == current.getBegin() - (recent.getBegin() + recent.getDuration()));
		}

	}
}
