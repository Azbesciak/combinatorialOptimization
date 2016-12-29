package algorithm;

import model.Maintenance;
import model.Operation;
import model.Task;
import model.wrapper.Instance;
import org.junit.jupiter.api.Test;
import service.MaintenanceService;
import service.TaskService;
import service.UtilsService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class AntTest {

	@Test
	public void prepareAntRandomPathTest() throws Exception {
		Ant ant = new Ant();

		int amount = 100;
		List<Task> tasks = TaskService.generateTasks(amount, 40);
		List<Maintenance> maintenances = MaintenanceService.generateMaintenances(50, 20, 20);
		Instance instance = ant.prepareAntPath(100, tasks, maintenances, null);

		int allEndsSum = instance.getTasks().stream().mapToInt(t -> t.getFirst().getEnd() + t.getSecond().getEnd())
				.sum();
		assertAll("AntTest", () -> assertTrue(instance.getQuality() == allEndsSum),
				() -> assertTrue(allEndsSum > 0),
				() -> assertTrue(instance.getTasks().size() == amount),
				() -> assertTrue(instance.getTasks().stream().filter(t -> t.getFirst().getBegin() == 0).count() <= 1));
	}

	@Test
	public void prepareAntKnownPathTest() throws Exception {
		Ant ant = new Ant();
		int amount = 5;
		int maxDuration = 10;
		double evaporationRatio = 0.8;

		PheromoneMatrix pheromoneMatrix = new PheromoneMatrix(amount, evaporationRatio);
		List<Maintenance> maintenances = MaintenanceService.generateMaintenances(50, 5, 20);
		List<Task> tasksForMatrix = prepareTestTasks(amount, maxDuration);
		pheromoneMatrix.updateMatrix(tasksForMatrix, 1);
		List<Task> newTasks = prepareTestTasks(amount, maxDuration);

		Instance instance = ant.prepareAntPath(0, newTasks, maintenances, pheromoneMatrix);
		pheromoneMatrix.updateMatrix(instance.getTasks(), 1);
		int allEndsSum = instance.getTasks().stream().mapToInt(t -> t.getFirst().getEnd() + t.getSecond().getEnd())
				.sum();
		assertAll("AntTest", () -> assertTrue(instance.getQuality() == allEndsSum),
				() -> assertTrue(allEndsSum > 0),
				() -> assertTrue(instance.getTasks().size() == amount),
				() -> assertTrue(instance.getTasks().stream().filter(t -> t.getFirst().getBegin() == 0).count() <= 1));
	}

	private List<Task> prepareTestTasks(int amount, int maxDuration) {
		Task.resetIndexer();
		Random random = new Random();
		List<Task> tasks = new ArrayList<>();
		for (int i = 0; i < amount; i++) {
			Operation operationOne = Operation.createFirstMachineOperation(random.nextInt(maxDuration), 0);
			Operation operationTwo = Operation.createSecondMachineOperation(random.nextInt(maxDuration));
			Task task = new Task(operationOne, operationTwo);
			tasks.add(task);
		}
		Task.resetIndexer();
		return tasks;
	}
}
