package service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rits.cloning.Cloner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class UtilsService {
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
			if (!directory.mkdir()) throw new IOException();
		}
		String persistencePath = directory.getCanonicalPath();
		persistencePath += File.separatorChar;

		return persistencePath;
	}

	public static void writeLineToFile(FileOutputStream writer, String line) throws IOException {
		line += '\n';
		writer.write(line.getBytes());
	}
}
