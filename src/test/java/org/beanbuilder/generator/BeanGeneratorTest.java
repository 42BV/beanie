package org.beanbuilder.generator;

import org.beanbuilder.generate.BeanGenerator;
import org.beanbuilder.sample.NestedBean;
import org.beanbuilder.sample.NestedBeanWithConstructor;
import org.beanbuilder.sample.SimpleBean;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BeanGeneratorTest {

	private BeanGenerator beanGenerator;
	
	@Before
	public void setUp() {
		beanGenerator = new BeanGenerator();
	}
	
	@Test
	public void testGenerate() {
		SimpleBean bean = (SimpleBean) beanGenerator.generate(SimpleBean.class);
		Assert.assertNotNull(bean);
		Assert.assertNotNull(bean.getValue());
		
		NestedBean nestedBean = bean.getNestedBean();
		Assert.assertNotNull(nestedBean);
		Assert.assertNotNull(nestedBean.getValue());
		
		NestedBeanWithConstructor nestedBeanWithConstructor = bean.getNestedBeanWithConstructor();
		Assert.assertNotNull(nestedBeanWithConstructor);
		Assert.assertNotNull(nestedBeanWithConstructor.getValue());
	}
	
	@Test
    public void testCustomProperty() {
		NestedBeanWithConstructor nestedBeanWithConstructor = new NestedBeanWithConstructor("bla");
		beanGenerator.registerValue(SimpleBean.class, "nestedBeanWithConstructor", nestedBeanWithConstructor);

		SimpleBean bean = (SimpleBean) beanGenerator.generate(SimpleBean.class);
		Assert.assertEquals(nestedBeanWithConstructor, bean.getNestedBeanWithConstructor());
	}
	
	@Test
    public void testCustomType() {
		SimpleBean bean = new SimpleBean();
		beanGenerator.registerValue(SimpleBean.class, bean);
		
		Assert.assertEquals(bean, beanGenerator.generate(SimpleBean.class));
	}
	
}
