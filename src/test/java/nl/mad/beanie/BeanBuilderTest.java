package nl.mad.beanie;

import nl.mad.beanie.domain.NestedBean;
import nl.mad.beanie.domain.NestedBeanWithConstructor;
import nl.mad.beanie.domain.SimpleBean;
import nl.mad.beanie.domain.SomeAbstract;
import nl.mad.beanie.domain.SomeImplementation;
import nl.mad.beanie.domain.SomeInterface;
import nl.mad.beanie.generator.BeanGenerator;
import nl.mad.beanie.generator.ConstantValueGenerator;
import nl.mad.beanie.generator.FirstImplBeanGenerator;
import nl.mad.beanie.generator.random.RandomStringGenerator;

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
    public void testGenerateWithCustomProperty() {
		NestedBeanWithConstructor nestedBeanWithConstructor = new NestedBeanWithConstructor("bla");
        beanBuilder.registerValue(SimpleBean.class, "nestedBeanWithConstructor", nestedBeanWithConstructor);

        SimpleBean bean = beanBuilder.start(SimpleBean.class).generateValues("nestedBeanWithConstructor").construct();
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
                                        .setValue("id", 42L)
                                        .generateValue("name", new ConstantValueGenerator("success"))
                                        .fill()
                                            .construct();
        
        Assert.assertEquals(Long.valueOf(42), bean.getId());
        Assert.assertEquals("success", bean.getName());
        Assert.assertNotNull(bean.getNestedBean());
        Assert.assertNotNull(bean.getNestedBeanWithConstructor());
    }
    
    @Test
    public void testBuildWithCopy() {
        beanBuilder.skip(SimpleBean.class, "id");
        
        SimpleBean bean = beanBuilder.start(SimpleBean.class)
                                        .setValue("id", 42L)
                                        .setValue("name", "Jan")
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
                                        .setId(42L)
                                        .setName(new ConstantValueGenerator("success"))
                                        .setNestedBean()
                                            .construct();
        
        Assert.assertEquals(Long.valueOf(42), bean.getId());
        Assert.assertNull(bean.getShortName());
        Assert.assertEquals("success", bean.getName());
        Assert.assertNotNull(bean.getNestedBean());
    }
    
    @Test
    public void testBuildWithDefinedGenerators() {
        beanBuilder.register(SimpleBean.class, "name", new RandomStringGenerator(2, 4));
        
        SimpleBean bean = beanBuilder.start(SimpleBean.class)
                                        .setValue("id", 42L)
                                        .fill()
                                            .construct();
        
        Assert.assertEquals(Long.valueOf(42), bean.getId());
        Assert.assertNotNull(bean.getName());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testBuildAndSaveUnsupported() {
        beanBuilder.start(SimpleBean.class).save();
    }

}
