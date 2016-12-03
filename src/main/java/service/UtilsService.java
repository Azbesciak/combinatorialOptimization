package service;

import com.rits.cloning.Cloner;


public class UtilsService {
	public static <E> E deepClone(E original) {
		return new Cloner().deepClone(original);
	}
}
