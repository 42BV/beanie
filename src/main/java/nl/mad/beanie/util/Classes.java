package nl.mad.beanie.util;

import java.lang.reflect.Constructor;
import java.util.Arrays;

public class Classes {

	@SuppressWarnings("unchecked")
	public static <T> Class<T> forName(String className) {
		try {
			return (Class<T>) Class.forName(className);
		} catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Could find class '" + className + "' on classpath.", e);
		}
	}

	public static boolean hasNullaryConstructor(Class<?> clazz) {
		return hasConstructor(clazz);
	}

	private static boolean hasConstructor(Class<?> clazz, Class<?>... types) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            if (Arrays.equals(types, constructor.getParameterTypes())) {
                return true;
            }
        }
        return false;
	}

}
