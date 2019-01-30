package nl._42.beanie.support;

import nl._42.beanie.util.Classes;

import org.junit.Assert;
import org.junit.Test;

public class ClassesTest {
	
	@Test
	public void testForName() {
		Assert.assertEquals(getClass(), Classes.forName(getClass().getName()));
	}

	@Test(expected = RuntimeException.class)
	public void testForNameNonExisting() {
		Classes.forName("my.unknown.Class");
	}
	
	@Test
	public void testHasNullaryConstructor() {
		Assert.assertTrue(Classes.hasNullaryConstructor(getClass()));
	}
	
}
