package nl._42.beanie;

import io.beanmapper.BeanMapper;
import io.beanmapper.config.BeanMapperBuilder;
import nl._42.beanie.convert.BeanMapperConverter;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.collections.Sets;

import java.util.Collections;

public class BeanBuilderTest {

    private BeanBuilder beanBuilder;

    @BeforeEach
    public void setUp() {
        beanBuilder = new BeanBuilder();
        beanBuilder.register(new AnnotationSupportable(SimpleAnnotation.class), new SimplePropertyValueGenerator());

        BeanMapper beanMapper = new BeanMapperBuilder().setApplyStrictMappingConvention(false).build();
        beanBuilder.setBeanConverter(new BeanMapperConverter(beanMapper));
	}

	@Test
	public void testGenerate() {
        SimpleBean bean = beanBuilder.generateSafely(SimpleBean.class);
        Assertions.assertNotNull(bean);

        Assertions.assertNotNull(bean.getName());
        Assertions.assertEquals("another 'annotated'", bean.getAnnotated());

        NestedBean nestedBean = bean.getNestedBean();
        Assertions.assertNotNull(nestedBean);
        Assertions.assertNotNull(nestedBean.getValue());

        NestedBeanWithConstructor nestedBeanWithConstructor = bean.getNestedBeanWithConstructor();
        Assertions.assertNotNull(nestedBeanWithConstructor);
        Assertions.assertNotNull(nestedBeanWithConstructor.getValue());
    }

    @Test
    public void testGenerateWithCustomType() {
        beanBuilder.registerValue(String.class, "success");

        SimpleBean bean = (SimpleBean) beanBuilder.generate(SimpleBean.class);
        Assertions.assertEquals("success", bean.getName());
    }

    @Test
    public void testGenerateFromClone() {
        beanBuilder.registerValue(String.class, "success");

        SimpleBean bean = (SimpleBean) new BeanBuilder(beanBuilder).generate(SimpleBean.class);
        Assertions.assertEquals("success", bean.getName());
    }

    @Test
    public void testGenerateWithCustomProperty() {
        NestedBeanWithConstructor nestedBeanWithConstructor = new NestedBeanWithConstructor("bla");
        beanBuilder.registerValue(SimpleBean.class, "nestedBeanWithConstructor", nestedBeanWithConstructor);

        SimpleBean bean = beanBuilder.start(SimpleBean.class).generateValue("nestedBeanWithConstructor").construct();
        Assertions.assertEquals(nestedBeanWithConstructor, bean.getNestedBeanWithConstructor());
    }

    @Test
    public void testGenerateInterfaceWithProxy() {
        SomeInterface someInterface = beanBuilder.generateSafely(SomeInterface.class);
        Assertions.assertNotNull(someInterface);
    }

    @Test
    public void testGenerateAbstractWithProxy() {
        SomeAbstract someAbstract = beanBuilder.generateSafely(SomeAbstract.class);
        Assertions.assertNotNull(someAbstract);
    }

    @Test
    public void testGenerateWithFirstImplementation() {
        BeanGenerator beanGenerator = beanBuilder.getBeanGenerator();
        beanGenerator.setAbstractGenerator(new FirstImplBeanGenerator(beanGenerator));

        SomeAbstract someAbstract = beanBuilder.generateSafely(SomeAbstract.class);
        Assertions.assertEquals(SomeImplementation.class, someAbstract.getClass());
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

        Assertions.assertEquals(Long.valueOf(42), bean.getId());
        Assertions.assertEquals("success", bean.getName());
        Assertions.assertEquals(Sets.newSet("coding", "gaming"), bean.getHobbies());
        Assertions.assertNotNull(bean.getNestedBean());
        Assertions.assertNotNull(bean.getNestedBeanWithConstructor());
    }

    @Test
    public void testBuildWithDefaultBuilderAndExistingBean() {
        SimpleBean base = new SimpleBean();
        base.setName("bla");

        SimpleBean bean = beanBuilder.start(base)
                .withValue("id", 42L)
                .fill()
                .construct();

        Assertions.assertEquals(Long.valueOf(42), bean.getId());
        Assertions.assertEquals("bla", bean.getName());
        Assertions.assertNotNull(bean.getNestedBean());
        Assertions.assertNotNull(bean.getNestedBeanWithConstructor());
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

        Assertions.assertEquals(Long.valueOf(42), bean.getId());
        Assertions.assertEquals("bla", bean.getName());
        Assertions.assertEquals(0, bean.getHobbies().size());
        Assertions.assertNotNull(bean.getNestedBean());
        Assertions.assertNotNull(bean.getNestedBeanWithConstructor());
    }

    @Test
    public void testBuildWithDefaultBuilderAndUnknownProperty() {
        SimpleBean base = new SimpleBean();

        Assertions.assertThrows(IllegalArgumentException.class, () ->
            beanBuilder.start(base).withValue("unknown", "crash")
        );
    }

    @Test
    public void testBuildWithDefinedGenerators() {
        beanBuilder.register(SimpleBean.class, "name", new RandomStringGenerator(2, 4));

        SimpleBean bean = beanBuilder.start(SimpleBean.class)
                .withValue("id", 42L)
                .fill()
                .construct();

        Assertions.assertEquals(Long.valueOf(42), bean.getId());
        Assertions.assertNotNull(bean.getName());
    }

    @Test
    public void testBuildWithLoadFromOther() {
        beanBuilder.skip(SimpleBean.class, "id");

        SimpleBean bean = beanBuilder.start(SimpleBean.class)
                .withValue("id", 42L)
                .withValue("name", "Jan")
                .fill()
                .construct();

        Assertions.assertEquals("Jan", bean.getName());

        SimpleBean clone = beanBuilder.start(SimpleBean.class)
                .load(bean, "shortName")
                .construct();

        // Copied from the simple bean
        Assertions.assertEquals("Jan", clone.getName());
        Assertions.assertEquals(bean.getNestedBean(), clone.getNestedBean());
        Assertions.assertEquals(bean.getNestedBeanWithConstructor(), clone.getNestedBeanWithConstructor());

        // Bean builder has skipped id, thus is not copied either
        Assertions.assertNull(clone.getId());

        // Marked as exclusion
        Assertions.assertNull(clone.getShortName());
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

        Assertions.assertEquals(Long.valueOf(42), bean.getId());
        Assertions.assertNull(bean.getShortName());
        Assertions.assertEquals("success", bean.getName());
        Assertions.assertEquals(Sets.newSet("coding"), bean.getHobbies());
        Assertions.assertNotNull(bean.getNestedBean());
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

        Assertions.assertEquals(Long.valueOf(42), bean.getId());
        Assertions.assertNull(bean.getShortName());
        Assertions.assertEquals("bla", bean.getName());
        Assertions.assertNotNull(bean.getNestedBean());
    }

    @Test
    public void testBuildWithCustomBuilderAndDefaultMethod() {
        SimpleBean bean = beanBuilder.startAs(SimpleBeanBuildCommand.class)
                .useDefaultName()
                .withHobbies("test")
                .construct();

        Assertions.assertEquals("Default", bean.getName());
        Assertions.assertEquals(Collections.singleton("test"), bean.getHobbies());
    }

    @Test
    public void testBuildWithCustomBuilderAndDefaultMethodWithArguments() {
        SimpleBean bean = beanBuilder.startAs(SimpleBeanBuildCommand.class)
          .withName("Jan", "de Boer")
          .withHobbies("test")
          .construct();

        Assertions.assertEquals("Jan de Boer", bean.getName());
        Assertions.assertEquals(Collections.singleton("test"), bean.getHobbies());
    }

    @Test
    public void testBuildWithCustomBuilderAndOtherConvention() {
        SimpleBean bean = beanBuilder.startAs(SetSimpleBeanBuildCommand.class)
                .setName("success")
                .withValue("id", 42L)
                .construct();

        Assertions.assertEquals(Long.valueOf(42), bean.getId());
        Assertions.assertEquals("success", bean.getName());
    }

    @Test
    public void testBuildAndSaveUnsupported() {
        BeanBuilder unsupported = new BeanBuilder(beanBuilder);
        unsupported.setBeanSaver(new UnsupportedBeanSaver());

        Assertions.assertThrows(UnsupportedOperationException.class, () ->
            unsupported.start(SimpleBean.class).save()
        );
    }

    @Test
    public void testInvalidMethods() {
        Assertions.assertThrows(UnsupportedOperationException.class, () ->
            beanBuilder.startAs(InvalidSimpleBeanBuildCommand.class)
        );
    }
    
    @Test
    public void testFacade() {
        SimpleBeanBuilder builder = new SimpleBeanBuilder(beanBuilder);

        SimpleBean bean = builder.start().fill().construct();
        Assertions.assertNotNull(bean);
        Assertions.assertNotNull(bean.getName());
    }
    
    @Test
    public void testFacadeWrap() {
        SimpleBean wrapped = new SimpleBean();
        wrapped.setName("abc");

        WrappedBeanBuilder<SimpleBean, SimpleBeanBuildCommand> builder =
          new WrappedBeanBuilder(beanBuilder, SimpleBeanBuildCommand.class);

        SimpleBean bean = builder.wrap(wrapped).fill().construct();
        Assertions.assertNotNull(bean);
        Assertions.assertEquals("abc", bean.getName());
    }

    // With mapping

    @Test
    public void testMapAndConstruct() {
        SimpleBeanResult bean = beanBuilder.startAs(SetSimpleBeanBuildCommand.class)
                .setName("success")
                .withValue("id", 42L)
                .map(SimpleBeanResult.class)
                .construct();

        Assertions.assertEquals(Long.valueOf(42), bean.id);
    }

    @Test
    public void testMapAndWith() {
        SimpleBeanResult beanResult = beanBuilder.startAs(SetSimpleBeanBuildCommand.class)
                .setName("success")
                .withValue("id", 42L)
                .map(SimpleBeanResultBuildCommand.class, SimpleBeanResult.class)
                .withUniqueId("Awesome")
                .construct();

        Assertions.assertEquals(Long.valueOf(42), beanResult.id);
        Assertions.assertEquals("Awesome", beanResult.uniqueId);
    }

}