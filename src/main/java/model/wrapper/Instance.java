package model.wrapper;

import model.Maintenance;
import model.Task;

import java.util.List;

public class Instance {

	private List<Task> tasks;
	private List<Maintenance> maintenances;

	private int getInitialSchedulingTime = -1;

	public Instance(List<Task> tasks, List<Maintenance> maintenances) {
		this.tasks = tasks;
		this.maintenances = maintenances;
		setInitialSchedulingTime();
	}

	public List<Task> getTasks() {
		return tasks;
	}

	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}

	public List<Maintenance> getMaintenances() {
		return maintenances;
	}

	public void setMaintenances(List<Maintenance> maintenances) {
		this.maintenances = maintenances;
	}

	public int getInitialSchedulingTime() {
		setInitialSchedulingTime();
		return getInitialSchedulingTime;
	}
	private void setInitialSchedulingTime() {
		if (getInitialSchedulingTime == -1) {
			getInitialSchedulingTime = getCurrentSchedulingTime();
		}
	}

	public int getCurrentSchedulingTime() {
		int max = -1;
		if (tasks != null) {
			for (Task task : tasks) {
				int maxFromTask = Math.max(task.getFirst().getEnd(), task.getSecond().getEnd());
				if (max < maxFromTask)
					max = maxFromTask;
			}
		}
		return max;
	}

	@Override
	public String toString() {
		return "Instance{" +
				"tasks=" + tasks +
				", maintenances=" + maintenances +
				'}';
	}
}
