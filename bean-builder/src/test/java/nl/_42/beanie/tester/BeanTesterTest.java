package nl._42.beanie.tester;

import nl._42.beanie.domain.FullBean;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BeanTesterTest {

	private BeanTester beanTester;
	
	@Before
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
		Assert.assertTrue("Expected atleast one bean to be verified.", verified > 0);
	}

	@Test
	public void testSkipInherit() {
        beanTester.inherit(false).verifyBean(FullBean.class);
	}
	
	@Test(expected = InconsistentGetterAndSetterException.class)
	public void testInconsistentProperty() {
		beanTester.verifyProperty(FullBean.class, "differentValue");
	}
	
	@Test(expected = InconsistentGetterAndSetterException.class)
	public void testInconsistentPropertyType() {
		beanTester.verifyProperty(FullBean.class, "differentTypeValue");
	}
	
	@Test(expected = IllegalStateException.class)
	public void testExceptionProperty() {
		beanTester.verifyProperty(FullBean.class, "unsupportedValue");
	}

}
