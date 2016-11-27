package service;

import exception.NonNegativeArgException;
import model.Operation;
import model.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TaskService {

    public static List<Task> generateTasks(int amount, int longestTime) throws NonNegativeArgException {
        if (longestTime <= 0 ) {
            throw new NonNegativeArgException("Task time must be positive!");
        }
        if (amount <= 0) {
            throw new NonNegativeArgException("Task amount must be positive!");
        }
        List<Task> tasks = new ArrayList<>();
        for (int i = 0; i < amount; i++) {

            int firstTaskDuration = new Random().nextInt((longestTime - 1)) + 1;
            Operation first = new Operation(firstTaskDuration);

            int secondTimeDuration = new Random().nextInt((longestTime - 1)) + 1;
            Operation second = new Operation(secondTimeDuration);

            Task task = new Task(first, second);
            tasks.add(task);
        }
        return tasks;
    }

    public static int getTotalTasksDuration(List<Task> tasks) {
        int totalTime = 0;
        if (tasks != null) {
            for (Task task : tasks) {
                totalTime += Math.max(task.getFirst().getDuration(), task.getSecond().getDuration());
            }
        }
        return totalTime;
    }

}
