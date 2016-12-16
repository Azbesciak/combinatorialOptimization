package model.wrapper;

import com.google.gson.Gson;
import enumeration.Machine;
import model.Maintenance;
import model.Operation;
import model.Task;
import model.abstracts.Event;
import service.UtilsService;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static service.UtilsService.writeLineToFile;

public class Solution {

	private static String SOLUTIONS_DIRECTORY = "solutions";
	private Instance instance;

	public Solution(Instance instance) {
		this.instance = instance;
	}

	public Instance getInstance() {
		return instance;
	}

	public void setInstance(Instance instance) {
		this.instance = instance;
	}

	public void persistSolution(String persistenceId) throws IOException {
		String schedulingTime = getSchedulingTime();
		List<Maintenance> maintenances = instance.getMaintenances();
		Iterator<Maintenance> maintenanceIterator = maintenances.iterator();
		StringBuilder machineOneSB = new StringBuilder("M1: ");
		List<TimeLineEvent> machineOneEvents = prepareTimeLineForMachine(Machine.ONE, maintenanceIterator);
		for (TimeLineEvent machineOneEvent : machineOneEvents) {
			machineOneSB.append(machineOneEvent);
		}
		StringBuilder machineTwoSB = new StringBuilder("M2: ");
		Iterator<Maintenance> dummyIterator = new ArrayList<Maintenance>().iterator();
		List<TimeLineEvent> machineTwoEvents = prepareTimeLineForMachine(Machine.TWO, dummyIterator);
		for (TimeLineEvent machineTwoEvent : machineTwoEvents) {
			machineTwoSB.append(machineTwoEvent);
		}
		String maintenancesLine = prepareMaintenancesLine();
		String machineOneIdle = prepareIdleLineForMachine(machineOneEvents);
		String machineTwoIdle = prepareIdleLineForMachine(machineTwoEvents);
		String solutionFile = getSolutionsDirectory() + persistenceId;
		try (FileOutputStream writer = new FileOutputStream(solutionFile)) {
			writeLineToFile(writer, "***** " + persistenceId + " *****");
			writeLineToFile(writer, schedulingTime);
			writeLineToFile(writer, machineOneSB.toString());
			writeLineToFile(writer, machineTwoSB.toString());
			writeLineToFile(writer, maintenancesLine);
			writeLineToFile(writer, "0");
			writeLineToFile(writer, machineOneIdle);
			writeLineToFile(writer, machineTwoIdle);
			writeLineToFile(writer, "*** EOF ***");
			writer.flush();
		}
	}

	private String getSchedulingTime() {
		int initialSchedulingTime = instance.getInitialSchedulingTime();
		int currentSchedulingTime = instance.getCurrentSchedulingTime();
		return initialSchedulingTime + ", " + currentSchedulingTime;
	}

	private String prepareMaintenancesLine() {
		List<Maintenance> maintenances = instance.getMaintenances();
		int maintenancesAmount = maintenances.size();
		int maintenancesTotalDuration = maintenances.stream().mapToInt(Event::getDuration).sum();
		return maintenancesAmount + ", " + maintenancesTotalDuration;
	}
	private String prepareIdleLineForMachine(List<TimeLineEvent> machineEvents) {
		int idleCounter = 0;
		int idleTotalTime = 0;
		System.out.println("machine events size : " + machineEvents.size());
		for (TimeLineEvent machineEvent : machineEvents) {
			if (EventType.IDLE.equals(machineEvent.eventType)) {
				idleCounter++;
				idleTotalTime += machineEvent.getDuration();
			}
		}
		return idleCounter + ", " + idleTotalTime;
	}

	public List<TimeLineEvent> prepareTimeLineForMachine(Machine machine, Iterator<Maintenance> maintenanceIterator) {
		List<TimeLineEvent> events = new ArrayList<>();
		Maintenance nextMaintenance = null;
		int maintenanceCounter = 0;
		int idleCounter = 0;
		if (maintenanceIterator.hasNext()) {
			nextMaintenance = maintenanceIterator.next();
		}

		List<Task> tasks = instance.getTasks();
		Operation recentOperation = null;
		Maintenance recentMaintenance = null;
		for (Task task : tasks) {
			Operation currentOperation;
			int operationNumber;
			if (machine.equals(task.getFirst().getMachine())) {
				currentOperation = task.getFirst();
				operationNumber = 1;
			} else {
				currentOperation = task.getSecond();
				operationNumber = 2;
			}
			if (recentOperation == null) {
				idleCounter = checkInitialIdleness(events, nextMaintenance, currentOperation);
			}
			boolean maintenanceOccurredBeforeOperation = false;
			while (nextMaintenance != null && nextMaintenance.getBegin() < currentOperation.getBegin()) {
				if (!maintenanceOccurredBeforeOperation && recentOperation != null) {
					idleCounter = checkIdleExistence(events, recentOperation, nextMaintenance, idleCounter);
				} else if (maintenanceOccurredBeforeOperation){
					idleCounter = checkIdleExistence(events, recentMaintenance, nextMaintenance, idleCounter);
				}
				maintenanceOccurredBeforeOperation = true;
				TimeLineEvent timeLineEvent =
						new TimeLineEvent(nextMaintenance, EventType.MAINTENANCE, maintenanceCounter++);
				events.add(timeLineEvent);
				recentMaintenance = nextMaintenance;
				if (maintenanceIterator.hasNext()) {
					nextMaintenance = maintenanceIterator.next();
				} else {
					nextMaintenance = null;
				}
			}
			if (recentMaintenance != null && maintenanceOccurredBeforeOperation) {
				idleCounter = checkIdleExistence(events, recentMaintenance, currentOperation, idleCounter);
			}
			TimeLineEvent timeLineEvent = new TimeLineEvent(currentOperation, EventType.OPERATION, operationNumber,
					task.getId());
			events.add(timeLineEvent);
			recentOperation = currentOperation;
		}
		return events;
	}

	private int checkInitialIdleness(List<TimeLineEvent> events, Maintenance firstMaintenance, Operation firstOperation) {
		if (firstMaintenance != null && firstMaintenance.getBegin() < firstOperation.getBegin()) {
			TimeLineEvent timeLineEvent =
					new TimeLineEvent(0, firstMaintenance.getBegin(), EventType.IDLE, 0);
			events.add(timeLineEvent);
			return 1;
		} else if (firstOperation.getBegin() > 0) {
			TimeLineEvent timeLineEvent =
					new TimeLineEvent(0, firstOperation.getBegin(), EventType.IDLE, 0);
			events.add(timeLineEvent);
			return 1;
		}
		return 0;
	}

	private int checkIdleExistence(List<TimeLineEvent> events, Event recent, Event current, int idleCounter) {
		int idleTime = current.getBegin() - recent.getEnd();
		if (idleTime > 0) {
			TimeLineEvent timeLineEvent = new TimeLineEvent(recent.getEnd(), idleTime,
					EventType.IDLE, idleCounter++);
			events.add(timeLineEvent);
		}
		return idleCounter;
	}

	private static String getSolutionsDirectory() throws IOException {
		return UtilsService.getDirectory(SOLUTIONS_DIRECTORY);
	}

	public class TimeLineEvent extends Event {
		private String identifier;
		EventType eventType;

		public TimeLineEvent(Event event, EventType eventType, int operationNumber, int taskId) {
			super(event.getDuration());
			setBegin(event.getBegin());
			this.eventType = eventType;
			setIdentifier(operationNumber, taskId);
		}

		public TimeLineEvent(Event event, EventType eventType, int operationNumber) {
			this(event.getBegin(), event.getDuration(), eventType, operationNumber);
		}

		public TimeLineEvent(int begin, int duration, EventType eventType, int operationNumber) {
			super(duration);
			setBegin(begin);
			this.eventType = eventType;
			setIdentifier(operationNumber);
		}

		private void setIdentifier(int operationNumber, int taskId) {
			setIdentifier(operationNumber);
			identifier += "_" + taskId;
		}

		private void setIdentifier(int operationNumber) {
			identifier = eventType.toString() + operationNumber;
		}

		@Override
		public String toString() {
			return identifier + ", " + getBegin() + ", " + getDuration() + "; ";
		}
	}

	private enum EventType {
		IDLE("idle"), MAINTENANCE("maint"), OPERATION("op");

		private final String name;

		EventType(String name) {
			this.name = name;
		}

		public String toString() {
			return this.name;
		}
	}

}
