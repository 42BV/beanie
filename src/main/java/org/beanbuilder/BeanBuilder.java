/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package org.beanbuilder;

import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.beanbuilder.generator.BeanGenerator;
import org.beanbuilder.generator.ConfigurableValueGenerator;
import org.beanbuilder.generator.ConstantValueGenerator;
import org.beanbuilder.generator.DefaultValueGenerator;
import org.beanbuilder.generator.ValueGenerator;
import org.beanbuilder.save.BeanSaver;
import org.beanbuilder.save.UnsupportedBeanSaver;
import org.beanbuilder.support.PropertyReference;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.target.SingletonTargetSource;
import org.springframework.core.GenericTypeResolver;

/**
 * Builds new bean instances.
 *
 * @author Jeroen van Schagen
 * @since Feb 14, 2014
 */
public class BeanBuilder implements ValueGenerator {
    
    /**
     * Collection of properties that should be skipped.
     */
    private final Set<PropertyReference> skippedProperties = new HashSet<>();
    
    /**
     * Property specific value generators.
     */
    private final Map<PropertyReference, ValueGenerator> propertyGenerators = new HashMap<>();
    
    /**
     * Type specific value generators.
     */
    private final ConfigurableValueGenerator typeGenerator;
    
    /**
     * Generator used to generate the result beans.
     */
    final BeanGenerator beanGenerator;
    
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
     * Start building a new bean.
     * 
     * @param beanClass the type of bean to start building
     * @return the bean build command
     */
    public <T> EditableBeanBuildCommand<T> start(Class<T> beanClass) {
        return new DefaultBeanBuildCommand<T>(this, beanClass);
    }
    
    /**
     * Start building a new bean, using a custom builder interface.
     * 
     * @param interfaceType the build command interface
     * @return the builder instance, capable of building beans
     */
    @SuppressWarnings("unchecked")
    public <T extends BeanBuildCommand<?>> T startAs(Class<T> interfaceType) {
        Class<?> beanClass = GenericTypeResolver.resolveTypeArguments(interfaceType, BeanBuildCommand.class)[0];
        EditableBeanBuildCommand<?> instance = start(beanClass);
        return startAs(interfaceType, instance);
    }
    
    /**
     * Start building a new bean, using a custom builder interface.
     * 
     * @param interfaceType the build command interface
     * @param instance the default command instance
     * @return the builder instance, capable of building beans
     */
    @SuppressWarnings("unchecked")
    public <T extends BeanBuildCommand<?>> T startAs(Class<T> interfaceType, EditableBeanBuildCommand<?> instance) {
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setTargetSource(new SingletonTargetSource(instance));
        proxyFactory.addInterface(interfaceType);
        proxyFactory.addAdvisor(new BeanBuildCommandAdvisor(instance));
        return (T) proxyFactory.getProxy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object generate(Class<?> beanClass) {
        if (typeGenerator.contains(beanClass)) {
            return typeGenerator.generate(beanClass);
        }
        return start(beanClass).fill().build();
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
        ValueGenerator generator = getGenerator(beanClass, descriptor);
        try {
            return generator.generate(descriptor.getPropertyType());
        } catch (RuntimeException rte) {
            throw new IllegalStateException("Could not generate property '" + descriptor.getName() + "' for: " + beanClass.getName(), rte);
        }
    }

    private ValueGenerator getGenerator(Class<?> beanClass, PropertyDescriptor descriptor) {
        ValueGenerator generator = this;
        PropertyReference reference = new PropertyReference(beanClass, descriptor.getName());
        if (propertyGenerators.containsKey(reference)) {
            generator = propertyGenerators.get(reference);
        } else if (typeGenerator.contains(descriptor.getPropertyType())) {
            generator = typeGenerator;
        }
        return generator;
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

}
