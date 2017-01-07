package repository;

import com.google.gson.Gson;
import com.sun.istack.internal.NotNull;
import model.Maintenance;
import model.Task;
import model.wrapper.Instance;
import service.UtilsService;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InstanceRepository {

//	private static final String INSTANCES_JSON_DIRECTORY = "testDir/instances/json";
	private static final String INSTANCES_JSON_DIRECTORY = "instances/json";
//	private static final String INSTANCES_READABLE_DIRECTORY = "testDir/instances/read";
	private static final String INSTANCES_READABLE_DIRECTORY = "instances/read";
	private static final String INSTANCE_DEFAULT_ID_FORMAT = "{0}_T{1}_M{2}";

	private InstanceRepository() {
		throw new UnsupportedOperationException();
	}


	public static String persistInstance(Instance instance) throws IOException {
		String fileName = getDefaultName(instance);
		persistInstance(instance, fileName);
		return fileName;
	}

	public static void persistInstance(Instance instance, @NotNull String customName) throws IOException {
		persistInstanceToJson(instance, customName);
		persistInstanceToFormat(instance, customName);
	}


	public static void persistInstanceToJson(final Instance instance, final String fileName) throws IOException {
		String instancePersistenceDirectory = getInstancesJsonDirectory();
		String instancePersistencePath = getNewPersistenceFilePath(instancePersistenceDirectory, fileName);
		try (FileOutputStream writer = new FileOutputStream(instancePersistencePath)) {
			String jsonInstance = new Gson().toJson(instance);
			writer.write(jsonInstance.getBytes());
		}
	}

	public static void persistInstanceToFormat(final Instance instance, final String fileName) throws IOException {
		String instancePersistenceDirectory = getInstancesReadableDirectory();
		String instancePersistencePath = getNewPersistenceFilePath(instancePersistenceDirectory, fileName);
		instancePersistencePath += ".txt";
		try (FileOutputStream writer = new FileOutputStream(instancePersistencePath)) {
			int tasksSize = instance.getTasks().size();
			UtilsService
					.writeAllLinesToFile(writer, "***** " + fileName + " *****", "tasks " + String.valueOf(tasksSize));
			List<Task> tasks = instance.getTasks();
			for (Task task : tasks) {
				String line = task.getFirst().getDuration() + "; " + task.getSecond().getDuration() + "; " + task
						.getFirst().getReadyTime() + ";";
				UtilsService.writeLineToFile(writer, line);
			}
			List<Maintenance> maintenances = instance.getMaintenances();
			UtilsService.writeLineToFile(writer, "maintenances " + maintenances.size());
			int maintenanceNumber = 0;
			for (Maintenance maintenance : maintenances) {
				String line = ++maintenanceNumber + "; 1; " + maintenance.getDuration() + "; " + maintenance.getBegin();
				UtilsService.writeLineToFile(writer, line);
			}
			UtilsService.writeLineToFile(writer, "***** EOF *****");

		}
	}

	private static String getNewPersistenceFilePath(String directory, String customName) {
		return directory + customName;
	}

	private static String getDefaultName(final Instance instance) {
		int tasksSize = instance.getTasks().size();
		int maintenancesSize = instance.getMaintenances().size();
		LocalDate localDate = LocalDateTime.now().toLocalDate();
		return MessageFormat.format(INSTANCE_DEFAULT_ID_FORMAT, localDate, tasksSize, maintenancesSize);
	}

	public static List<Path> listAllInstances() throws IOException {
		String dir = getInstancesJsonDirectory();
		List<Path> paths = new ArrayList<>();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(dir))) {
			for (Path entry : stream) {
				paths.add(entry);
			}
		}
		return paths;
	}

	public static Instance getInstance(Path path) throws IOException {
		try (Stream<String> stream = Files.lines(path)) {
			String jsonInstance = stream.collect(Collectors.joining());
			return new Gson().fromJson(jsonInstance, Instance.class);
		}
	}

	private static String getInstancesJsonDirectory() throws IOException {
		return UtilsService.getDirectory(INSTANCES_JSON_DIRECTORY);
	}

	private static String getInstancesReadableDirectory() throws IOException {
		return UtilsService.getDirectory(INSTANCES_READABLE_DIRECTORY);
	}

}
