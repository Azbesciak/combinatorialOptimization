package service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rits.cloning.Cloner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


public class UtilsService {
	private final static DecimalFormat decimalFormat= new DecimalFormat("#0.00");
	private UtilsService() {
		throw new UnsupportedOperationException();
	}

	public static <E> E deepClone(E original) {
		return new Cloner().deepClone(original);
	}

	public static <E> void printPrettyJson(E toPrint) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String s = gson.toJson(toPrint);
		System.out.printf(s);
	}

	public static String getDirectory(String name) throws IOException {
		File directory = new File("." + File.separator + name);
		if (!directory.isDirectory()) {
			if (!directory.mkdirs()) throw new IOException();
		}
		String persistencePath = directory.getCanonicalPath();
		persistencePath += File.separatorChar;

		return persistencePath;
	}

	public static void writeLineToFile(FileOutputStream writer, String line) throws IOException {
		line += System.lineSeparator();
		writer.write(line.getBytes());
	}

	public static boolean writeAllLinesToFile(FileOutputStream writer, String... lines) throws IOException {
		if (lines != null) {
			for (String line : lines) {

				line += System.lineSeparator();
				writer.write(line.getBytes());
			}
			return true;
		}
		return false;
	}

	public static List<Path> getAllFilesInDirectoryByName(String dir, String name) throws IOException {
		List<Path> paths = new ArrayList<>();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(dir))) {
			for (Path entry : stream) {
				if (entry.getFileName().toString().contains(name)) {
					paths.add(entry);
				}
			}
		}
		return paths;
	}

	public static void showProgress(int progress, int total, String message) {
		String anim= "|/-\\";
		double value = progress * 100 / (double) total;
		String data = "\r\033[34m" + anim.charAt(progress % anim.length())  + " " + decimalFormat.format(value) + "%";
			if (message != null) {
				data += " \033[32m" + message + "\033[38m";
			}
		try {
			System.out.write(data.getBytes());
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}

}
