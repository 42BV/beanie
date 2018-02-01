package nl._42.beanie;

import io.beanmapper.config.BeanMapperBuilder;
import nl._42.beanie.domain.NestedBean;
import nl._42.beanie.domain.NestedBeanWithConstructor;
import nl._42.beanie.domain.SimpleBean;
import nl._42.beanie.domain.SimpleBeanResult;
import nl._42.beanie.domain.SomeAbstract;
import nl._42.beanie.domain.SomeImplementation;
import nl._42.beanie.domain.SomeInterface;
import nl._42.beanie.generator.BeanGenerator;
import nl._42.beanie.generator.ConstantValueGenerator;
import nl._42.beanie.generator.FirstImplBeanGenerator;
import nl._42.beanie.generator.random.RandomStringGenerator;
import nl._42.beanie.generator.supported.AnnotationSupportable;
import nl._42.beanie.save.UnsupportedBeanSaver;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.collections.Sets;

public class BeanBuilderTest {

    private BeanBuilder beanBuilder;

    @Before
    public void setUp() {
        beanBuilder = new BeanBuilder();
        beanBuilder.register(new AnnotationSupportable(SimpleAnnotation.class), new SimplePropertyValueGenerator());
        beanBuilder.setBeanMapper(new BeanMapperBuilder().setApplyStrictMappingConvention(false).build());
	}

	@Test
	public void testGenerate() {
        SimpleBean bean = beanBuilder.generateSafely(SimpleBean.class);
        Assert.assertNotNull(bean);

        Assert.assertNotNull(bean.getName());
        Assert.assertEquals("another 'annotated'", bean.getAnnotated());

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
                .withValue("hobbies", "gaming")
                .generateValue("name", new ConstantValueGenerator("success"))
                .fill()
                .construct();

        Assert.assertEquals(Long.valueOf(42), bean.getId());
        Assert.assertEquals("success", bean.getName());
        Assert.assertEquals(Sets.newSet("coding", "gaming"), bean.getHobbies());
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

    @Test(expected = IllegalArgumentException.class)
    public void testBuildWithDefaultBuilderAndUnknownProperty() {
        SimpleBean base = new SimpleBean();
        beanBuilder.start(base).withValue("unknown", "crash");
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
                .perform(x -> x.getNestedBean().setValue("abc"))
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
                .perform(x -> x.getNestedBean().setValue("abc"))
                .map(x -> x)
                .withValue("id", 42L)
                .construct();

        Assert.assertEquals(Long.valueOf(42), bean.getId());
        Assert.assertNull(bean.getShortName());
        Assert.assertEquals("bla", bean.getName());
        Assert.assertNotNull(bean.getNestedBean());
    }

    @Test
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
        BeanBuilder unsupported = new BeanBuilder(beanBuilder);
        unsupported.setBeanSaver(new UnsupportedBeanSaver());
        unsupported.start(SimpleBean.class).save();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testInvalidMethods() {
        beanBuilder.startAs(InvalidSimpleBeanBuildCommand.class);
    }
    
    @Test
    public void testFacade() {
        SimpleBeanBuilder builder = new SimpleBeanBuilder(beanBuilder);

        SimpleBean bean = builder.start().fill().construct();
        Assert.assertNotNull(bean);
        Assert.assertNotNull(bean.getName());
    }

    @Test
    public void testFacadeWrap() {
        SimpleBean wrapped = new SimpleBean();
        wrapped.setName("abc");
        
        SimpleBeanBuilder builder = new SimpleBeanBuilder(beanBuilder);
        SimpleBean bean = builder.wrap(wrapped).fill().construct();
        Assert.assertNotNull(bean);
        Assert.assertEquals("abc", bean.getName());
    }
    
    @Test
    public void testFacadeGeneric() {
        WrappedBeanBuilder<SimpleBean, SimpleBeanBuildCommand> builder = new WrappedBeanBuilder<>(SimpleBeanBuildCommand.class, beanBuilder);

        SimpleBean bean = builder.start().fill().construct();
        Assert.assertNotNull(bean);
        Assert.assertNotNull(bean.getName());
    }

    // With mapping

    @Test
    public void testMapAndConstruct() {
        SimpleBeanResult bean = beanBuilder.startAs(SetSimpleBeanBuildCommand.class)
                .setName("success")
                .withValue("id", 42L)
                .map(SimpleBeanResult.class)
                .construct();

        Assert.assertEquals(Long.valueOf(42), bean.id);
    }

    @Test
    public void testMapAndWith() {
        SimpleBeanResult beanResult = beanBuilder.startAs(SetSimpleBeanBuildCommand.class)
                .setName("success")
                .withValue("id", 42L)
                .map(SimpleBeanResultBuildCommand.class, SimpleBeanResult.class)
                .withUniqueId("Awesome")
                .construct();

        Assert.assertEquals(Long.valueOf(42), beanResult.id);
        Assert.assertEquals("Awesome", beanResult.uniqueId);
    }

}