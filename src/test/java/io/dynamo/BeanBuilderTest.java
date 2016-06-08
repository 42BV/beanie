package io.dynamo;

import io.dynamo.domain.NestedBean;
import io.dynamo.domain.NestedBeanWithConstructor;
import io.dynamo.domain.SimpleBean;
import io.dynamo.domain.SomeAbstract;
import io.dynamo.domain.SomeImplementation;
import io.dynamo.domain.SomeInterface;
import io.dynamo.generator.BeanGenerator;
import io.dynamo.generator.ConstantValueGenerator;
import io.dynamo.generator.FirstImplBeanGenerator;
import io.dynamo.generator.random.RandomStringGenerator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.collections.Sets;

public class BeanBuilderTest {

    private BeanBuilder beanBuilder;
	
	@Before
	public void setUp() {
        beanBuilder = new BeanBuilder();
	}
	
	@Test
	public void testGenerate() {
        SimpleBean bean = beanBuilder.generateSafely(SimpleBean.class);
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
    public void testGenerateFromClone() {
        beanBuilder.registerValue(String.class, "success");
        
        SimpleBean bean = (SimpleBean) new BeanBuilder(beanBuilder).generate(SimpleBean.class);
        Assert.assertEquals("success", bean.getName());
    }

	@Test
    public void testGenerateWithCustomProperty() {
		NestedBeanWithConstructor nestedBeanWithConstructor = new NestedBeanWithConstructor("bla");
        beanBuilder.registerValue(SimpleBean.class, "nestedBeanWithConstructor", nestedBeanWithConstructor);

        SimpleBean bean = beanBuilder.start(SimpleBean.class).generateValue("nestedBeanWithConstructor").construct();
		Assert.assertEquals(nestedBeanWithConstructor, bean.getNestedBeanWithConstructor());
	}

    @Test
    public void testGenerateInterfaceWithProxy() {
        SomeInterface someInterface = beanBuilder.generateSafely(SomeInterface.class);
        Assert.assertNotNull(someInterface);
    }
    
    @Test
    public void testGenerateAbstractWithProxy() {
        SomeAbstract someAbstract = beanBuilder.generateSafely(SomeAbstract.class);
        Assert.assertNotNull(someAbstract);
    }
    
    @Test
    public void testGenerateWithFirstImplementation() {
        BeanGenerator beanGenerator = beanBuilder.getBeanGenerator();
        beanGenerator.setAbstractGenerator(new FirstImplBeanGenerator(beanGenerator));
        
        SomeAbstract someAbstract = beanBuilder.generateSafely(SomeAbstract.class);
        Assert.assertEquals(SomeImplementation.class, someAbstract.getClass());
    }

    @Test
    public void testBuildWithDefaultBuilder() {
        SimpleBean bean = beanBuilder.start(SimpleBean.class)
                                        .withValue("id", 42L)
                                        .withValue("hobbies", "coding")
                                        .generateValue("name", new ConstantValueGenerator("success"))
                                        .fill()
                                            .construct();
        
        Assert.assertEquals(Long.valueOf(42), bean.getId());
        Assert.assertEquals("success", bean.getName());
        Assert.assertEquals(Sets.newSet("coding"), bean.getHobbies());
        Assert.assertNotNull(bean.getNestedBean());
        Assert.assertNotNull(bean.getNestedBeanWithConstructor());
    }

    @Test
    public void testBuildWithDefaultBuilderAndExistingBean() {
        SimpleBean base = new SimpleBean();
        base.setName("bla");
        
        SimpleBean bean = beanBuilder.start(base)
                                        .withValue("id", 42L)
                                        .fill()
                                            .construct();
        
        Assert.assertEquals(Long.valueOf(42), bean.getId());
        Assert.assertEquals("bla", bean.getName());
        Assert.assertNotNull(bean.getNestedBean());
        Assert.assertNotNull(bean.getNestedBeanWithConstructor());
    }

    @Test
    public void testBuildWithDefaultBuilderSkipEmptyValueToCollection() {
        SimpleBean base = new SimpleBean();
        base.setName("bla");
        
        SimpleBean bean = beanBuilder.start(base)
                                        .withValue("id", 42L)
                                        .withValue("hobbies", null)
                                        .fill()
                                            .construct();
        
        Assert.assertEquals(Long.valueOf(42), bean.getId());
        Assert.assertEquals("bla", bean.getName());
        Assert.assertNull(bean.getHobbies());
        Assert.assertNotNull(bean.getNestedBean());
        Assert.assertNotNull(bean.getNestedBeanWithConstructor());
    }
    
    @Test
    public void testBuildWithDefinedGenerators() {
        beanBuilder.register(SimpleBean.class, "name", new RandomStringGenerator(2, 4));
        
        SimpleBean bean = beanBuilder.start(SimpleBean.class)
                                        .withValue("id", 42L)
                                        .fill()
                                            .construct();
        
        Assert.assertEquals(Long.valueOf(42), bean.getId());
        Assert.assertNotNull(bean.getName());
    }

    @Test
    public void testBuildWithLoadFromOther() {
        beanBuilder.skip(SimpleBean.class, "id");
        
        SimpleBean bean = beanBuilder.start(SimpleBean.class)
                                        .withValue("id", 42L)
                                        .withValue("name", "Jan")
                                        .fill()
                                            .construct();
                    
        Assert.assertEquals("Jan", bean.getName());

        SimpleBean clone = beanBuilder.start(SimpleBean.class)
                                        .load(bean, "shortName")
                                            .construct();
        
        // Copied from the simple bean
        Assert.assertEquals("Jan", clone.getName());
        Assert.assertEquals(bean.getNestedBean(), clone.getNestedBean());
        Assert.assertEquals(bean.getNestedBeanWithConstructor(), clone.getNestedBeanWithConstructor());
        
        // Bean builder has skipped id, thus is not copied either
        Assert.assertNull(clone.getId());
        
        // Marked as exclusion
        Assert.assertNull(clone.getShortName());
    }
    
    @Test
    public void testBuildWithCustomBuilder() {  
        SimpleBean bean = beanBuilder.startAs(SimpleBeanBuildCommand.class)
                                        .withName(new ConstantValueGenerator("success"))
                                        .withNestedBean()
                                        .withHobbies("coding")
                                        .doWith(x -> x.getNestedBean().setValue("abc"))
                                        .map(x -> x)
                                        .withValue("id", 42L)
                                            .construct();
        
        Assert.assertEquals(Long.valueOf(42), bean.getId());
        Assert.assertNull(bean.getShortName());
        Assert.assertEquals("success", bean.getName());
        Assert.assertEquals(Sets.newSet("coding"), bean.getHobbies());
        Assert.assertNotNull(bean.getNestedBean());
    }
    
    @Test
    public void testBuildWithCustomBuilderAndExistingBean() {
        SimpleBean base = new SimpleBean();
        base.setName("bla");
        
        SimpleBean bean = beanBuilder.startAs(SimpleBeanBuildCommand.class, base)
                                        .withNestedBean()
                                        .doWith(x -> x.getNestedBean().setValue("abc"))
                                        .map(x -> x)
                                        .withValue("id", 42L)
                                            .construct();
        
        Assert.assertEquals(Long.valueOf(42), bean.getId());
        Assert.assertNull(bean.getShortName());
        Assert.assertEquals("bla", bean.getName());
        Assert.assertNotNull(bean.getNestedBean());
    }
    
    // TODO: Cannot get this to work: no private access for invokespecial
    // URL: https://rmannibucau.wordpress.com/2014/03/27/java-8-default-interface-methods-and-jdk-dynamic-proxies
    @Test(expected = UnsupportedOperationException.class)
    public void testBuildWithCustomBuilderAndDefaultMethod() {        
        SimpleBean bean = beanBuilder.startAs(SimpleBeanBuildCommand.class)
                                        .useDefaultName()
                                            .construct();
        
        Assert.assertEquals("Default", bean.getName());
    }
    
    @Test
    public void testBuildWithCustomBuilderAndOtherConvention() {        
        SimpleBean bean = beanBuilder.startAs(SetSimpleBeanBuildCommand.class)
                                        .setName("success")
                                        .withValue("id", 42L)
                                            .construct();
        
        Assert.assertEquals(Long.valueOf(42), bean.getId());
        Assert.assertEquals("success", bean.getName());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testBuildAndSaveUnsupported() {
        beanBuilder.start(SimpleBean.class).save();
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void testInvalidMethods() {
        beanBuilder.startAs(InvalidSimpleBeanBuildCommand.class);
    }

}
