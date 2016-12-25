package model.wrapper;

import model.Maintenance;
import model.Task;

import java.util.List;

public class Instance {

	private List<Task> tasks;
	private List<Maintenance> maintenances;
	private int quality;
	private int initialSchedulingTime = -1;

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
		return initialSchedulingTime;
	}

	public void setInitialSchedulingTime(int schedulingTime) {
		initialSchedulingTime = schedulingTime;
	}

	private void setInitialSchedulingTime() {
		if (initialSchedulingTime == -1) {
			initialSchedulingTime = getCurrentSchedulingTime();
		}
	}

	public int getCurrentSchedulingTime() {
		if (tasks != null) {
			return tasks.stream()
					.mapToInt(t -> t.getFirst().getEnd() + t.getSecond().getEnd())
					.sum();
		}
		return -1;
	}

	@Override
	public String toString() {
		return "Instance{" +
				"tasks=" + tasks +
				", maintenances=" + maintenances +
				'}';
	}

	public int getQuality() {
		return quality;
	}

	public void setQuality(int quality) {
		this.quality = quality;
	}
}
