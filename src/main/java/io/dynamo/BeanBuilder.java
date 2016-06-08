package io.dynamo;

import io.dynamo.generator.BeanGenerator;
import io.dynamo.generator.ConstantValueGenerator;
import io.dynamo.generator.DefaultValueGenerator;
import io.dynamo.generator.TypeBasedValueGenerator;
import io.dynamo.generator.ValueGenerator;
import io.dynamo.generator.supported.PredicateSupportable;
import io.dynamo.generator.supported.Supportable;
import io.dynamo.save.BeanSaver;
import io.dynamo.save.UnsupportedBeanSaver;
import io.dynamo.util.PropertyReference;
import io.dynamo.util.Proxies;

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

import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.core.GenericTypeResolver;
import org.springframework.util.ReflectionUtils;

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
    
    /**
     * Saves the generated beans.
     */
    private final BeanSaver beanSaver;

    /**
     * Construct a new {@link BeanBuilder}.
     * <br><br>
     * <b>Note that using this constructor means the beans cannot be saved</b>
     */
    public BeanBuilder() {
        this(new UnsupportedBeanSaver());
    }

    /**
     * Construct a new {@link BeanBuilder}.
     * 
     * @param beanSaver responsible for saving the bean after creation
     */
    public BeanBuilder(BeanSaver beanSaver) {
        this.typeGenerator = new DefaultValueGenerator(this);
        this.beanGenerator = new BeanGenerator(this);
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
     * @param beanClass the type of bean to start building
     * @return the bean build command
     */
    public <T> EditableBeanBuildCommand<T> start(Class<T> beanClass) {
        return new DefaultBeanBuildCommand<T>(this, beanClass);
    }

    /**
     * Start building a new bean.
     * 
     * @param bean the initial bean
     * @return the bean build command
     */
    public <T> EditableBeanBuildCommand<T> start(T bean) {
        return new DefaultBeanBuildCommand<T>(this, bean);
    }
    
    /**
     * Start building a new bean, using a custom builder interface.
     * 
     * @param interfaceType the build command interface
     * @return the builder instance, capable of building beans
     */
    public <T extends BeanBuildCommand<?>> T startAs(Class<T> interfaceType) {
        final Class<?> beanClass = GenericTypeResolver.resolveTypeArguments(interfaceType, BeanBuildCommand.class)[0];
        return wrapToInterface(interfaceType, start(beanClass));
    }
    
    /**
     * Start building a new bean, using a custom builder interface.
     * 
     * @param interfaceType the build command interface
     * @return the builder instance, capable of building beans
     */
    public <T extends BeanBuildCommand<B>, B> T startAs(Class<T> interfaceType, B bean) {
        return wrapToInterface(interfaceType, start(bean));
    }

    @SuppressWarnings("unchecked")
    private <T extends BeanBuildCommand<?>> T wrapToInterface(Class<T> interfaceType, EditableBeanBuildCommand<?> instance) {
        final BeanBuildConfig annotation = interfaceType.getAnnotation(BeanBuildConfig.class);
        final String preffix = annotation != null ? annotation.preffix() : WITH_PREFIX;

        validate(preffix, interfaceType);

        DefaultPointcutAdvisor advisor = buildAdvisor(preffix, instance);
        return (T) Proxies.wrapAsProxy(interfaceType, instance, advisor);
    }
    
    private void validate(String preffix, Class<?> interfaceType) {
        Method[] methods = interfaceType.getDeclaredMethods();
        for (Method method : methods) {
            if (!((method.getName().startsWith(preffix)) || method.isDefault())) {
                throw new UnsupportedOperationException("Interface methods should start with '" + preffix + "' or be default.");
            }
        }
    }

    private DefaultPointcutAdvisor buildAdvisor(String preffix, EditableBeanBuildCommand<?> instance) {
        BeanBuilderPointcut pointcut = new BeanBuilderPointcut(preffix);
        BeanBuildCommandAdvice advice = new BeanBuildCommandAdvice(instance, preffix);
        return new DefaultPointcutAdvisor(pointcut, advice);
    }

    private static class BeanBuilderPointcut extends StaticMethodMatcherPointcut {
        
        private final String preffix;

        public BeanBuilderPointcut(String preffix) {
            this.preffix = preffix;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean matches(Method method, Class<?> targetClass) {
            return (method.getName().startsWith(preffix) && method.getParameterCount() <= 1) || method.isDefault();
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
        return start(beanClass).fill().construct();
    }

    /**
     * Generate a new bean, properly casted to the correct type.
     * 
     * @param beanClass the bean class
     * @return the generated bean
     */
    @SuppressWarnings("unchecked")
    public <T> T generateSafely(Class<T> beanClass) {
        return (T) generate(beanClass);
    }

    Object generateValue(Class<?> beanClass, PropertyDescriptor descriptor) {
        PropertyReference reference = new PropertyReference(beanClass, descriptor.getName());
        Class<?> propertyType = descriptor.getPropertyType();
        ValueGenerator generator = findGenerator(reference, propertyType);
        
        try {
            // Provides the property reference during generation
            if (generator instanceof PropertyValueGenerator) {
                return ((PropertyValueGenerator) generator).generate(reference, propertyType);
            }
            return generator.generate(descriptor.getPropertyType());
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
                if (wrapper.supportable.supports(field)) {
                    return wrapper.generator;
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
     * @param bean the bean to save
     * @return the saved bean
     */
    public <R> R save(R bean) {
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
    
    private static class SupportableValueGenerators {
        
        private final ValueGenerator generator;
        
        private final Supportable supportable;
        
        public SupportableValueGenerators(ValueGenerator generator, Supportable supportable) {
            this.generator = generator;
            this.supportable = supportable;
        }
        
    }

}
