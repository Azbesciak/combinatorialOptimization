package service;

import enumeration.EventType;
import enumeration.Machine;
import model.Maintenance;
import model.Operation;
import model.Task;
import model.abstracts.Event;
import model.wrapper.Instance;
import model.wrapper.TimeLineEvent;
import org.apache.commons.lang3.StringUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static service.UtilsService.writeAllLinesToFile;

public class SolutionService {

//	private static final String SOLUTIONS_DIRECTORY = "testDir/solutions";
	private static final String SOLUTIONS_DIRECTORY = "solutions";
	private static final String AUTO_RESULTS_SOLUTIONS_DIRECTORY = "autoResults";
//	private static final String AUTO_RESULTS_SOLUTIONS_DIRECTORY = "testDir/autoResults";

	private SolutionService() {
	}

	public static void persistSolutionsResults(Iterable<String> results, String header) throws Exception {
		String solutionsDirectory = getAutoResultsSolutionsDirectory();
		String localDate = LocalDateTime.now().toLocalDate().toString();
		String solutionFile = solutionsDirectory + localDate;
		int filesWithSameName = UtilsService.getAllFilesInDirectoryByName(solutionsDirectory, localDate).size();
		if (filesWithSameName > 0) {
			solutionFile += "(" + filesWithSameName + ")";
		}
		solutionFile += ".txt";
		try (FileOutputStream writer = new FileOutputStream(solutionFile)) {
			if (StringUtils.isNotBlank(header)) {
				UtilsService.writeLineToFile(writer, header);
			}
			for (String result : results) {
				UtilsService.writeLineToFile(writer, result);
			}
		}
	}

	public static void persistSolution(Instance instance, String persistenceId) throws IOException {
		String schedulingTime = getSchedulingTime(instance);
		List<Maintenance> maintenances = instance.getMaintenances();
		Iterator<Maintenance> maintenanceIterator = maintenances.iterator();
		StringBuilder machineOneSB = new StringBuilder("M1: ");
		List<TimeLineEvent> machineOneEvents = prepareTimeLineForMachine(Machine.ONE, maintenanceIterator, instance);
		for (TimeLineEvent machineOneEvent : machineOneEvents) {
			machineOneSB.append(machineOneEvent);
		}
		StringBuilder machineTwoSB = new StringBuilder("M2: ");
		Iterator<Maintenance> dummyIterator = new ArrayList<Maintenance>().iterator();
		List<TimeLineEvent> machineTwoEvents = prepareTimeLineForMachine(Machine.TWO, dummyIterator, instance);
		for (TimeLineEvent machineTwoEvent : machineTwoEvents) {
			machineTwoSB.append(machineTwoEvent);
		}
		String maintenancesLine = prepareMaintenancesLine(instance);
		String machineOneIdle = prepareIdleLineForMachine(machineOneEvents);
		String machineTwoIdle = prepareIdleLineForMachine(machineTwoEvents);
		String solutionsDirectory = getSolutionsDirectory();
		String solutionFile = solutionsDirectory + persistenceId;
		int filesWithSameName = UtilsService.getAllFilesInDirectoryByName(solutionsDirectory, persistenceId).size();
		if (filesWithSameName > 0) {
			solutionFile += "(" + filesWithSameName + ")";
		}
		solutionFile += ".txt";
		try (FileOutputStream writer = new FileOutputStream(solutionFile)) {
			writeAllLinesToFile(writer,
					"***** " + persistenceId + " *****",
					schedulingTime,
					machineOneSB.toString(),
					machineTwoSB.toString(),
					maintenancesLine,
					"0",
					machineOneIdle,
					machineTwoIdle,
					"*** EOF ***");
		}
	}

	private static String getSchedulingTime(Instance instance) {
		int initialSchedulingTime = instance.getInitialSchedulingTime();
		int currentSchedulingTime = instance.getQuality();
		return initialSchedulingTime + ", " + currentSchedulingTime;
	}

	private static String prepareMaintenancesLine(Instance instance) {
		List<Maintenance> maintenances = instance.getMaintenances();
		int maintenancesAmount = maintenances.size();
		int maintenancesTotalDuration = maintenances.stream().mapToInt(Event::getDuration).sum();
		return maintenancesAmount + ", " + maintenancesTotalDuration;
	}

	private static String prepareIdleLineForMachine(List<TimeLineEvent> machineEvents) {
		int idleCounter = 0;
		int idleTotalTime = 0;
		for (TimeLineEvent machineEvent : machineEvents) {
			if (EventType.IDLE.equals(machineEvent.getEventType())) {
				idleCounter++;
				idleTotalTime += machineEvent.getDuration();
			}
		}
		return idleCounter + ", " + idleTotalTime;
	}

	public static List<TimeLineEvent> prepareTimeLineForMachine(Machine machine,
																Iterator<Maintenance> maintenanceIterator,
																Instance instance) {
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
				} else if (maintenanceOccurredBeforeOperation) {
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
			} else if (recentOperation != null) {//due to readyTime
				idleCounter = checkIdleExistence(events, recentOperation, currentOperation, idleCounter);
			}
			TimeLineEvent timeLineEvent = new TimeLineEvent(currentOperation, EventType.OPERATION, operationNumber,
					task.getId());
			events.add(timeLineEvent);
			recentOperation = currentOperation;
		}
		return events;
	}

	private static int checkInitialIdleness(List<TimeLineEvent> events, Maintenance firstMaintenance,
											Operation firstOperation) {
		int idleBegin = -1;
		if (firstMaintenance != null && firstMaintenance.getBegin() < firstOperation.getBegin()) {
			idleBegin = firstMaintenance.getBegin();
		} else if (firstOperation.getBegin() > 0) {
			idleBegin = firstOperation.getBegin();
		}
		if (idleBegin >= 0) {
			TimeLineEvent timeLineEvent =
					new TimeLineEvent(0, idleBegin, EventType.IDLE, 0);
			events.add(timeLineEvent);
			return 1;
		}
		return 0;
	}

	private static int checkIdleExistence(List<TimeLineEvent> events, Event recent, Event current, int idleCounter) {
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

	private static String getAutoResultsSolutionsDirectory() throws IOException {
		return UtilsService.getDirectory(AUTO_RESULTS_SOLUTIONS_DIRECTORY);
	}
}
