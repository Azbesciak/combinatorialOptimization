package service;

import exception.NonNegativeArgException;
import model.Maintenance;
import model.Operation;
import model.Task;
import org.junit.After;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static org.junit.Assert.*;

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

    @Test(expected = NonNegativeArgException.class)
    public void generateTasksNegativeLongTime() throws Exception {
        int longestTime = 1;
        int amount = 0;
        List<Task> tasks = TaskService.generateTasks(amount, longestTime);
        fail("NonNegativeArgException should occur");
    }

    @Test(expected = NonNegativeArgException.class)
    public void generateTasksNegativeAmount() throws Exception {
        int longestTime = 0;
        int amount = 1;
        List<Task> tasks = TaskService.generateTasks(amount, longestTime);
        fail("NonNegativeArgException should occur");
    }

    @Test
    public void prepareMachineOperations() throws Exception {
        int longestTime = 15;
        int amount = 100;
        List<Task> tasks = TaskService.generateTasks(amount, longestTime);
        int totalTasksDuration = TaskService.getTotalTasksDuration(tasks);
        List<Maintenance> maintenances = MaintenanceService.generateMaintenances(totalTasksDuration, 10);
        Queue<Operation> awaitingOperations = new LinkedList<>();

        Class<TaskService> taskServiceClass = TaskService.class;
        Class<?>[] methodFirstArgsClasses = {List.class, List.class, Queue.class};
        Method prepareFirstMachineOperations = taskServiceClass.
                getDeclaredMethod("prepareFirstMachineOperations", methodFirstArgsClasses);
        prepareFirstMachineOperations.setAccessible(true);
        List<Operation> result = (List<Operation>) prepareFirstMachineOperations
                .invoke(null, tasks, maintenances, awaitingOperations);
        assertTrue(result.size() == awaitingOperations.size());
        assertTrue(result.size() == amount);

        Class<?>[] methodSecondArgsClasses = {List.class, Queue.class};
        Method prepareSecondMachineOperations = taskServiceClass.
                getDeclaredMethod("prepareSecondMachineOperations", methodSecondArgsClasses);
        prepareSecondMachineOperations.setAccessible(true);

        result = (List<Operation>) prepareSecondMachineOperations.
                invoke(null, result, awaitingOperations);
        assertTrue(awaitingOperations.isEmpty());
        assertTrue(result.size() == amount);
    }

}