package nl._42.beanie.tester;

import nl._42.beanie.domain.FullBean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BeanTesterTest {

	private BeanTester beanTester;
	
	@BeforeEach
	public void setUp() {
		beanTester = new BeanTester();
		beanTester.exclude(FullBean.class, "unsupportedValue");
		beanTester.exclude(FullBean.class, "differentValue");
		beanTester.exclude(FullBean.class, "differentTypeValue");
        beanTester.includeAllWithNullaryConstructor();
	}
	
	@Test
	public void testAllBeans() {		
        int verified = beanTester.verifyBeans(this.getClass());
		Assertions.assertTrue(verified > 0, "Expected atleast one bean to be verified.");
	}

	@Test
	public void testSkipInherit() {
        beanTester.inherit(false).verifyBean(FullBean.class);
	}
	
	@Test
	public void testInconsistentProperty() {
		Assertions.assertThrows(InconsistentGetterAndSetterException.class, () ->
			beanTester.verifyProperty(FullBean.class, "differentValue")
		);
	}
	
	@Test
	public void testInconsistentPropertyType() {
		Assertions.assertThrows(InconsistentGetterAndSetterException.class, () ->
			beanTester.verifyProperty(FullBean.class, "differentTypeValue")
		);
	}
	
	@Test
	public void testExceptionProperty() {
		Assertions.assertThrows(IllegalStateException.class, () ->
			beanTester.verifyProperty(FullBean.class, "unsupportedValue")
		);
	}

}
