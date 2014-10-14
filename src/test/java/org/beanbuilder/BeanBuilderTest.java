package org.beanbuilder;

import org.beanbuilder.BeanBuilder.BuildCommand;
import org.beanbuilder.domain.NestedBean;
import org.beanbuilder.domain.NestedBeanWithConstructor;
import org.beanbuilder.domain.SimpleBean;
import org.beanbuilder.domain.SomeImplementation;
import org.beanbuilder.domain.SomeInterface;
import org.beanbuilder.generator.ConstantValueGenerator;
import org.beanbuilder.generator.ValueGenerator;
import org.beanbuilder.generator.random.RandomStringGenerator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BeanBuilderTest {

    private BeanBuilder builder;
	
	@Before
	public void setUp() {
        builder = new BeanBuilder();
	}
	
	@Test
	public void testGenerate() {
        SimpleBean bean = builder.generateSafely(SimpleBean.class);
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
        builder.registerValue(String.class, "success");
        
        SimpleBean bean = (SimpleBean) builder.generate(SimpleBean.class);
        Assert.assertEquals("success", bean.getName());
    }

	@Test
    public void testGenerateWithCustomProperty() {
		NestedBeanWithConstructor nestedBeanWithConstructor = new NestedBeanWithConstructor("bla");
		builder.registerValue(SimpleBean.class, "nestedBeanWithConstructor", nestedBeanWithConstructor);

		SimpleBean bean = (SimpleBean) builder.generate(SimpleBean.class);
		Assert.assertEquals(nestedBeanWithConstructor, bean.getNestedBeanWithConstructor());
	}

    @Test
    public void testGenerateWithSomeImplementation() {
        SomeInterface someInterface = builder.generateSafely(SomeInterface.class);
        Assert.assertEquals(SomeImplementation.class, someInterface.getClass());
    }

    @Test
    public void testBuildWithDefaultBuilder() {
        SimpleBean bean = builder.newBean(SimpleBean.class)
                                        .withValue("id", 42L)
                                        .withGeneratedValue("name", new ConstantValueGenerator("success"))
                                            .fill()
                                            .build();
        
        Assert.assertEquals(Long.valueOf(42), bean.getId());
        Assert.assertEquals("success", bean.getName());
        Assert.assertNotNull(bean.getNestedBean());
        Assert.assertNotNull(bean.getNestedBeanWithConstructor());
    }
    
    @Test
    public void testBuildWithCustomBuilder() {        
        SimpleBean bean = builder.newBeanBy(SimpleBeanBuildCommand.class)
                                        .withId(42L)
                                        .withName(new ConstantValueGenerator("success"))
                                        .withNestedBean()
                                            .build();
        
        Assert.assertEquals(Long.valueOf(42), bean.getId());
        Assert.assertNull(bean.getShortName());
        Assert.assertEquals("success", bean.getName());
        Assert.assertNotNull(bean.getNestedBean());
    }
    
    @Test
    public void testBuildWithDefinedGenerators() {
        builder.register(SimpleBean.class, "name", new RandomStringGenerator(2, 4));
        
        SimpleBean bean = builder.newBean(SimpleBean.class).withValue("id", 42L).fill().build();
        Assert.assertEquals(Long.valueOf(42), bean.getId());
        Assert.assertNotNull(bean.getName());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testBuildAndSaveUnsupported() {
        builder.newBean(SimpleBean.class).buildAndSave();
    }

    public interface SimpleBeanBuildCommand extends BuildCommand<SimpleBean> {

        SimpleBeanBuildCommand withId(Long id);

        SimpleBeanBuildCommand withName(String name);
        
        SimpleBeanBuildCommand withName(ValueGenerator generator);

        SimpleBeanBuildCommand withNestedBean();

    }

}
