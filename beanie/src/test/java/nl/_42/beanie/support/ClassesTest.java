package nl._42.beanie.support;

import nl._42.beanie.util.Classes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ClassesTest {
	
	@Test
	public void testForName() {
		Assertions.assertEquals(getClass(), Classes.forName(getClass().getName()));
	}

	@Test
	public void testForNameNonExisting() {
		Assertions.assertThrows(RuntimeException.class, () ->
			Classes.forName("my.unknown.Class")
		);
	}
	
	@Test
	public void testHasNullaryConstructor() {
		Assertions.assertTrue(Classes.hasNullaryConstructor(getClass()));
	}
	
}
