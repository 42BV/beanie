package nl._42.beanie;

import nl._42.beanie.convert.BeanConverter;
import nl._42.beanie.convert.UnsupportedBeanConverter;
import nl._42.beanie.generator.BeanGenerator;
import nl._42.beanie.generator.ConstantValueGenerator;
import nl._42.beanie.generator.DefaultValueGenerator;
import nl._42.beanie.generator.PropertyValueGenerator;
import nl._42.beanie.generator.TypeBasedValueGenerator;
import nl._42.beanie.generator.ValueGenerator;
import nl._42.beanie.generator.supported.PredicateSupportable;
import nl._42.beanie.generator.supported.Supportable;
import nl._42.beanie.generator.supported.SupportableValueGenerators;
import nl._42.beanie.save.BeanSaver;
import nl._42.beanie.save.NoOperationBeanSaver;
import nl._42.beanie.util.PropertyReference;
import nl._42.beanie.util.Proxies;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.core.GenericTypeResolver;
import org.springframework.util.ReflectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Builds new bean instances.
 *
 * @author Jeroen van Schagen
 * @since Feb 14, 2014
 */
public class BeanBuilder implements ValueGenerator {
    
    private static final String WITH_PREFIX = "with";

    /**
     * Collection of properties that should be skipped.
     */
    private final Set<PropertyReference> skippedProperties = new HashSet<>();
    
    /**
     * Property specific value generators.
     */
    private final Map<PropertyReference, ValueGenerator> propertyGenerators = new HashMap<>();
    
    /**
     * Supported predicate specific value generators.
     */
    private final List<SupportableValueGenerators> supportedGenerators = new ArrayList<>();

    /**
     * Type specific value generators.
     */
    private final TypeBasedValueGenerator typeGenerator;
    
    /**
     * Generator used to generate the result beans.
     */
    private final BeanGenerator beanGenerator;

    private BeanConverter beanConverter;
    
    /**
     * Saves the generated beans.
     */
    private BeanSaver beanSaver;

    /**
     * Construct a simple {@link BeanBuilder}, please note that this
     * builder by default does not yet support conversions and persistence.
     *
     * @see #setBeanConverter(BeanConverter)
     * @see #setBeanSaver(BeanSaver)
     */
    public BeanBuilder() {
        this(new NoOperationBeanSaver());
    }

    /**
     * Construct a simple {@link BeanBuilder}, please note that this
     * builder by default does not yet support conversions.
     *
     * @see #setBeanConverter(BeanConverter)
     * @param beanSaver used to save constructed beans
     */
    public BeanBuilder(BeanSaver beanSaver) {
        this(new UnsupportedBeanConverter(), beanSaver);
    }

    /**
     * Construct a simple {@link BeanBuilder}.
     *
     * @param beanConverter used to convert beans between types
     * @param beanSaver used to save constructed beans
     */
    public BeanBuilder(BeanConverter beanConverter, BeanSaver beanSaver) {
        this.typeGenerator = new DefaultValueGenerator(this);
        this.beanGenerator = new BeanGenerator(this);
        this.beanConverter = beanConverter;
        this.beanSaver = beanSaver;
    }

    /**
     * Construct a new {@link BeanBuilder} cloning the settings
     * of an existing builder.
     * 
     * @param beanBuilder the builder instance to clone from
     */
    public BeanBuilder(BeanBuilder beanBuilder) {
        this.skippedProperties.addAll(beanBuilder.skippedProperties);
        this.propertyGenerators.putAll(beanBuilder.propertyGenerators);
        this.typeGenerator = beanBuilder.typeGenerator.clone();
        this.beanGenerator = beanBuilder.beanGenerator;
        this.beanSaver = beanBuilder.beanSaver;
    }

    /**
     * Start building a new bean.
     *
     * @param <T> the bean type
     * @param beanClass the type of bean to start building
     * @return the bean build command
     */
    public <T> EditableBeanBuildCommand<T> start(Class<T> beanClass) {
        return new DefaultBeanBuildCommand<>(this, beanClass, beanConverter);
    }

    /**
     * Start building a new bean.
     *
     * @param <T> the bean type
     * @param bean the initial bean
     * @return the bean build command
     */
    public <T> EditableBeanBuildCommand<T> start(T bean) {
        return new DefaultBeanBuildCommand<>(this, bean, beanConverter);
    }
    
    /**
     * Start building a new bean, using a custom builder interface.
     *
     * @param <I> the interface type
     * @param interfaceType the build command interface
     * @return the builder instance, capable of building beans
     */
    public <I extends BeanBuildCommand<?>> I startAs(Class<I> interfaceType) {
        final Class<?> beanClass = GenericTypeResolver.resolveTypeArguments(interfaceType, BeanBuildCommand.class)[0];
        return wrapToInterface(interfaceType, start(beanClass));
    }
    
    /**
     * Start building a new bean, using a custom builder interface.
     *
     * @param <I> the interface type
     * @param <T> the bean type
     * @param interfaceType the build command interface
     * @param bean the template bean
     * @return the builder instance, capable of building beans
     */
    public <I extends BeanBuildCommand<T>, T> I startAs(Class<I> interfaceType, T bean) {
        return wrapToInterface(interfaceType, start(bean));
    }

    @SuppressWarnings("unchecked")
    private <T extends BeanBuildCommand<?>> T wrapToInterface(Class<T> interfaceType, EditableBeanBuildCommand<?> instance) {
        final BeanBuilderConfig annotation = interfaceType.getAnnotation(BeanBuilderConfig.class);
        final String prefix = annotation != null ? annotation.preffix() : WITH_PREFIX;

        validate(prefix, interfaceType);

        BeanBuilderPointcut pointcut = new BeanBuilderPointcut(prefix);
        BeanBuildCommandAdvice advice = new BeanBuildCommandAdvice(instance, prefix);
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor(pointcut, advice);
        EditableBeanBuildCommand<?> proxy = Proxies.wrapAsProxy(interfaceType, instance, advisor);
        advice.setProxy(proxy); // Link back to proxy for default methods
        return (T) proxy;
    }
    
    private void validate(String preffix, Class<?> interfaceType) {
        Method[] methods = interfaceType.getDeclaredMethods();
        for (Method method : methods) {
            // @todo -- find a better solution to deal with Jacoco meddling
            if (method.getName().startsWith("$jacocoInit")) {
                continue; // Ignore this method
            }
            if (!((method.getName().startsWith(preffix)) || method.isDefault())) {
                throw new UnsupportedOperationException("Interface methods should start with '" + preffix + "' or be default.");
            }
        }
    }

    private static class BeanBuilderPointcut extends StaticMethodMatcherPointcut {
        
        private final String prefix;

        public BeanBuilderPointcut(String prefix) {
            this.prefix = prefix;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean matches(Method method, Class<?> targetClass) {
            return (method.getName().startsWith(prefix) && method.getParameterCount() <= 1) || method.isDefault();
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object generate(Class<?> beanClass) {
        if (typeGenerator.contains(beanClass)) {
            return typeGenerator.generate(beanClass);
        }
        return start(beanClass).fill().construct(true);
    }

    /**
     * Generate a new bean, properly casted to the correct type.
     *
     * @param <T> the bean type
     * @param beanClass the bean class
     * @return the generated bean
     */
    @SuppressWarnings("unchecked")
    public <T> T generateSafely(Class<T> beanClass) {
        return (T) generate(beanClass);
    }

    protected Object generateValue(Class<?> beanClass, PropertyDescriptor descriptor) {
        PropertyReference reference = new PropertyReference(beanClass, descriptor.getName());
        Class<?> propertyType = descriptor.getPropertyType();
        ValueGenerator generator = findGenerator(reference, propertyType);
        
        try {
            if (generator instanceof PropertyValueGenerator) {
                return ((PropertyValueGenerator) generator).generate(reference, propertyType);
            } else {
                return generator.generate(descriptor.getPropertyType());
            }
        } catch (RuntimeException rte) {
            throw new IllegalStateException("Could not generate property '" + descriptor.getName() + "' for: " + beanClass.getName(), rte);
        }
    }

    private ValueGenerator findGenerator(PropertyReference reference, Class<?> propertyType) {
        ValueGenerator generator = this;
        if (propertyGenerators.containsKey(reference)) {
            generator = propertyGenerators.get(reference);
        } else {
            ValueGenerator supportedGenerator = findSupportedGenerator(reference);
            if (supportedGenerator != null) {
                generator = supportedGenerator;
            } else if (typeGenerator.contains(propertyType)) {
                generator = typeGenerator;
            }
        }
        return generator;
    }
    
    private ValueGenerator findSupportedGenerator(PropertyReference property) {
        Field field = ReflectionUtils.findField(property.getDeclaringClass(), property.getPropertyName());
        if (field != null) {
            for (SupportableValueGenerators wrapper : supportedGenerators) {
                if (wrapper.getSupportable().supports(field)) {
                    return wrapper.getGenerator();
                }
            }
        }
        return null;
    }

    /**
     * Skip a property from being generated.
     * 
     * @param declaringClass the bean that declares this property
     * @param propertyName the property name
     * @return this instance
     */
    public BeanBuilder skip(Class<?> declaringClass, String propertyName) {
        skippedProperties.add(new PropertyReference(declaringClass, propertyName));
        return this;
    }

    /**
     * Register a value generation strategy for a specific property reference.
     * 
     * @param declaringClass the bean class that declares our property
     * @param propertyName the name of the property
     * @param generator the generation strategy
     * @return this instance
     */
    public BeanBuilder register(Class<?> declaringClass, String propertyName, ValueGenerator generator) {
        propertyGenerators.put(new PropertyReference(declaringClass, propertyName), generator);
        return this;
    }
    
    /**
     * Register a constant value for a specific property reference.
     * 
     * @param declaringClass the bean class that declares our property
     * @param propertyName the name of the property
     * @param value the value to return
     * @return this instance
     */
    public BeanBuilder registerValue(Class<?> declaringClass, String propertyName, Object value) {
        return register(declaringClass, propertyName, new ConstantValueGenerator(value));
    }

    /**
     * Register a value generation strategy for a specific type.
     * 
     * @param valueType the type of value
     * @param generator the generation strategy
     * @return this instance
     */
    public BeanBuilder register(Class<?> valueType, ValueGenerator generator) {
        typeGenerator.register(valueType, generator);
        return this;
    }
    
    /**
     * Register a value generation strategy for a specific type.
     * 
     * @param predicate the support predicate
     * @param generator the generation strategy
     * @return this instance
     */
    public BeanBuilder register(Supportable predicate, ValueGenerator generator) {
        supportedGenerators.add(new SupportableValueGenerators(generator, predicate));
        return this;
    }
    
    /**
     * Register a value generation strategy for a specific type.
     * 
     * @param predicate the support predicate
     * @param generator the generation strategy
     * @return this instance
     */
    public BeanBuilder registerIf(Predicate<AccessibleObject> predicate, ValueGenerator generator) {
        return register(new PredicateSupportable(predicate), generator);
    }
    
    /**
     * Register a constant value for a specific type.
     * 
     * @param valueType the type of value
     * @param value the value to return
     * @return this instance
     */
    public BeanBuilder registerValue(Class<?> valueType, Object value) {
        return register(valueType, new ConstantValueGenerator(value));
    }

    /**
     * Saves the bean.
     *
     * @param <T> the bean type
     * @param bean the bean to save
     * @return the saved bean
     */
    public <T> T save(T bean) {
        if (bean == null) {
            return null;
        }
        return beanSaver.save(bean);
    }

    /**
     * Deletes the bean.
     * 
     * @param bean the bean to delete
     */
    public void delete(Object bean) {
        beanSaver.delete(bean);
    }
    
    /**
     * Deletes multiple beans.
     * 
     * @param beans the beans to delete
     */
    public void deleteAll(Iterable<? extends Object> beans) {
        for (Object bean : beans) {
            beanSaver.delete(bean);
        }
    }
    
    /**
     * Retrieves the underlying bean generator.
     * 
     * @return the bean generator
     */
    public final BeanGenerator getBeanGenerator() {
        return beanGenerator;
    }
    
    /**
     * Retrieves the skipped properties.
     * 
     * @return the skipped properties
     */
    public Set<PropertyReference> getSkippedProperties() {
        return skippedProperties;
    }
    
    /**
     * @param beanSaver the saver to set
     */
    public void setBeanSaver(BeanSaver beanSaver) {
        this.beanSaver = beanSaver;
    }

    /**
     * @param beanConverter the converter to set
     */
    public void setBeanConverter(BeanConverter beanConverter) {
        this.beanConverter = beanConverter;
    }

}
