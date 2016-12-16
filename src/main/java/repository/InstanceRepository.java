package repository;

import com.google.gson.Gson;
import enumeration.Machine;
import model.wrapper.Instance;
import model.Operation;
import model.Task;
import model.abstracts.Event;
import service.UtilsService;

import java.io.File;
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

	private static String INSTANCES_DIRECTORY = "instances";

	private InstanceRepository() {
		throw new UnsupportedOperationException();
	}

	public static void persistInstance(Instance instance) throws IOException {
		persistInstance(instance, null);
	}

	public static void persistInstance(Instance instance, String customName) throws IOException {
		String instancePersistencePath = getInstancesDirectory();
		if (customName != null) {
			instancePersistencePath += customName;
		} else {
			int tasksSize = instance.getTasks().size();
			int maintenancesSize = instance.getMaintenances().size();
			LocalDate localDate = LocalDateTime.now().toLocalDate();
			String fileName = MessageFormat.format("{0}_T_{1}_M_{2}", localDate, tasksSize, maintenancesSize);
			instancePersistencePath += fileName;
		}
		try (FileOutputStream writer = new FileOutputStream(instancePersistencePath)) {
			String jsonInstance = new Gson().toJson(instance);
			writer.write(jsonInstance.getBytes());
			writer.flush();
		}
	}

	public static List<Path> listAllInstances() throws IOException {
		String dir = getInstancesDirectory();
		List<Path> paths = new ArrayList<>();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(dir))) {
			for (Path entry: stream) {
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

	private static String getInstancesDirectory() throws IOException {
		return UtilsService.getDirectory(INSTANCES_DIRECTORY);
	}

}
