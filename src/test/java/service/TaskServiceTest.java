package service;

import exception.NonNegativeArgException;
import model.Task;
import org.junit.Test;

import java.util.List;

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

}