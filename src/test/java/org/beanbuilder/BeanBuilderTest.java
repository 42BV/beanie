package org.beanbuilder;

import org.beanbuilder.BeanBuilder.BuildCommand;
import org.beanbuilder.domain.NestedBean;
import org.beanbuilder.domain.NestedBeanWithConstructor;
import org.beanbuilder.domain.SimpleBean;
import org.beanbuilder.domain.SomeImplementation;
import org.beanbuilder.domain.SomeInterface;
import org.beanbuilder.generator.ConstantValueGenerator;
import org.beanbuilder.generator.ValueGenerator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BeanBuilderTest {

    private BeanBuilder beanBuilder;
	
	@Before
	public void setUp() {
        beanBuilder = new BeanBuilder();
	}
	
	@Test
	public void testGenerate() {
		SimpleBean bean = (SimpleBean) beanBuilder.generate(SimpleBean.class);
		Assert.assertNotNull(bean);
        
        Assert.assertNotNull(bean.getName());
		
		NestedBean nestedBean = bean.getNestedBean();
		Assert.assertNotNull(nestedBean);
		Assert.assertNotNull(nestedBean.getValue());
		
		NestedBeanWithConstructor nestedBeanWithConstructor = bean.getNestedBeanWithConstructor();
		Assert.assertNotNull(nestedBeanWithConstructor);
		Assert.assertNotNull(nestedBeanWithConstructor.getValue());
	}
    
    @Test
    public void testGenerateWithCustomType() {
        beanBuilder.registerValue(String.class, "success");
        
        SimpleBean bean = (SimpleBean) beanBuilder.generate(SimpleBean.class);
        Assert.assertEquals("success", bean.getName());
    }

	@Test
    public void testGenerateWithCustomProperty() {
		NestedBeanWithConstructor nestedBeanWithConstructor = new NestedBeanWithConstructor("bla");
		beanBuilder.registerValue(SimpleBean.class, "nestedBeanWithConstructor", nestedBeanWithConstructor);

		SimpleBean bean = (SimpleBean) beanBuilder.generate(SimpleBean.class);
		Assert.assertEquals(nestedBeanWithConstructor, bean.getNestedBeanWithConstructor());
	}

    @Test
    public void testGenerateWithSomeImplementation() {
        SomeInterface someInterface = (SomeInterface) beanBuilder.generate(SomeInterface.class);
        Assert.assertEquals(SomeImplementation.class, someInterface.getClass());
    }

    @Test
    public void testBuildWithDefaultBuilder() {
        SimpleBean bean = beanBuilder.newBean(SimpleBean.class)
                                        .withValue("id", 42L)
                                        .withGeneratedValue("name", new ConstantValueGenerator("success"))
                                            .complete().build();
        
        Assert.assertEquals(Long.valueOf(42), bean.getId());
        Assert.assertEquals("success", bean.getName());
        Assert.assertNotNull(bean.getNestedBean());
        Assert.assertNotNull(bean.getNestedBeanWithConstructor());
    }
    
    @Test
    public void testBuildWithCustomBuilder() {        
        SimpleBean bean = beanBuilder.newBeanBy(SimpleBeanBuildCommand.class)
                                        .withId(42L)
                                        .withName(new ConstantValueGenerator("success"))
                                        .withNestedBean()
                                            .build();
        
        Assert.assertEquals(Long.valueOf(42), bean.getId());
        Assert.assertNull(bean.getShortName());
        Assert.assertEquals("success", bean.getName());
        Assert.assertNotNull(bean.getNestedBean());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testBuildAndSaveUnsupported() {
        beanBuilder.newBean(SimpleBean.class).buildAndSave();
    }

    public interface SimpleBeanBuildCommand extends BuildCommand<SimpleBean> {

        SimpleBeanBuildCommand withId(Long id);

        SimpleBeanBuildCommand withName(String name);
        
        SimpleBeanBuildCommand withName(ValueGenerator generator);

        SimpleBeanBuildCommand withNestedBean();

    }

}
