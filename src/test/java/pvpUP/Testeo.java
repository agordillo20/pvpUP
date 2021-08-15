package pvpUP;


import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import agordillo.pvpup.Manager;

public class Testeo {

	private static String sectionp = "asdf";
	public static void main(String[] args) {
		String conseguido = null;
		try {
			Field field = Testeo.class.getDeclaredField("section"+"p");
			field.setAccessible(true);
			
			conseguido = (String) field.get(String.class);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		System.out.println(conseguido);
	}

	
}
