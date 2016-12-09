package service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rits.cloning.Cloner;


public class UtilsService {
	public static <E> E deepClone(E original) {
		return new Cloner().deepClone(original);
	}

	public static <E> void printPrettyJson(E toPrint) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String s = gson.toJson(toPrint);
		System.out.printf(s);
	}
}
