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
import org.beanbuilder.generator.constructor.ConstructorStrategy;
import org.beanbuilder.generator.constructor.ShortestConstructorStrategy;
import org.beanbuilder.save.BeanSaver;
import org.beanbuilder.save.UnsupportedBeanSaver;
import org.beanbuilder.support.PropertyReference;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.target.SingletonTargetSource;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.DirectFieldAccessor;
import org.springframework.core.GenericTypeResolver;


/**
 * Builds new bean instances.
 *
 * @author Jeroen van Schagen
 * @since Feb 14, 2014
 */
public class BeanBuilder implements ValueGenerator {
    
    private final Set<PropertyReference> skippedProperties = new HashSet<>();
    
    private final Map<PropertyReference, ValueGenerator> propertyGenerators = new HashMap<>();
    
    private final ConfigurableValueGenerator typeGenerator;
    
    private final ValueGenerator generator;
    
    private final BeanSaver saver;

    public BeanBuilder() {
        this(new UnsupportedBeanSaver());
    }
    
    public BeanBuilder(BeanSaver beanSaver) {
        this(new ShortestConstructorStrategy(), beanSaver);
    }
    
    public BeanBuilder(ConstructorStrategy constructorStrategy, BeanSaver saver) {
        this.typeGenerator = new DefaultValueGenerator(this);
        this.generator = new BeanGenerator(constructorStrategy, this);
        this.saver = saver;
    }

    /**
     * Start building a new bean.
     * 
     * @param beanClass the type of bean to start building
     * @return the bean build command
     */
    public <T> ConfigurableBuildCommand<T> newBean(Class<T> beanClass) {
        return new DefaultBeanBuildCommand<T>(this, beanClass);
    }
    
    /**
     * Start building a new bean, using a custom builder interface.
     * 
     * @param type the type of bean to start building
     * @param commandType the build command interface
     * @return the builder instance, capable of building beans
     */
    @SuppressWarnings("unchecked")
    public <T extends BuildCommand<?>> T newBeanBy(Class<T> commandType) {
        final Class<?> beanClass = GenericTypeResolver.resolveTypeArguments(commandType, BuildCommand.class)[0];
        final ConfigurableBuildCommand<?> command = newBean(beanClass);

        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setTargetSource(new SingletonTargetSource(command));
        proxyFactory.addInterface(commandType);
        proxyFactory.addAdvisor(new CustomBeanBuilderAdvisor(command));
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
        return newBean(beanClass).fill().build();
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

    private Object generateValue(Class<?> beanClass, PropertyDescriptor descriptor) {
        ValueGenerator generator = getGenerator(beanClass, descriptor);
        return generator.generate(descriptor.getPropertyType());
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
     * @param valueType the type of value
     * @param value the value to return
     * @return this instance
     */
    public BeanBuilder registerValue(Class<?> valueType, Object value) {
        return register(valueType, new ConstantValueGenerator(value));
    }

    /**
     * Command for building beans.
     *
     * @author Jeroen van Schagen
     * @since Feb 14, 2014
     */
    public interface BuildCommand<T> {
        
        /**
         * Generate all untouched, changable, values in our bean.
         * 
         * @return this instance, for chaining
         */
        BuildCommand<T> fill();
        
        /**
         * Build the new bean.
         * 
         * @return the created bean
         */
        T build();
        
        /**
         * Build and save new bean.
         * 
         * @return the saved bean
         */
        T buildAndSave();

    }
    
    /**
     * Bean build command that allows users to declare custom property values.
     *
     * @author Jeroen van Schagen
     * @since Feb 14, 2014
     */
    public interface ConfigurableBuildCommand<T> extends BuildCommand<T> {
        
        /**
         * Generate a value in our to be generated bean.
         * 
         * @param propertyName the property name
         * @return this instance, for chaining
         */
        ConfigurableBuildCommand<T> withGeneratedValue(String propertyName);
        
        /**
         * Generate a value in our to be generated bean.
         * 
         * @param propertyName the property name
         * @param generator the value generator
         * @return this instance, for chaining
         */
        ConfigurableBuildCommand<T> withGeneratedValue(String propertyName, ValueGenerator generator);
        
        /**
         * Declare a value in our to be generated bean.
         * 
         * @param propertyName the property name
         * @param value the property value
         * @return this instance, for chaining
         */
        ConfigurableBuildCommand<T> withValue(String propertyName, Object value);

    }

    /**
     * Default implementation of the bean build command.
     *
     * @author Jeroen van Schagen
     * @since Feb 14, 2014
     */
    private static class DefaultBeanBuildCommand<T> implements ConfigurableBuildCommand<T> {

        private final Set<String> touchedProperties = new HashSet<>();
        
        private final Set<String> generatedProperties = new HashSet<>();
        
        private final BeanBuilder builder;

        private final BeanWrapper wrapper;

        private final DirectFieldAccessor fields;

        public DefaultBeanBuildCommand(BeanBuilder builder, Class<T> type) {
            this.builder = builder;

            Object bean = builder.generator.generate(type);
            wrapper = new BeanWrapperImpl(bean);
            fields = new DirectFieldAccessor(bean);
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public ConfigurableBuildCommand<T> withValue(String propertyName, Object value) {
            if (wrapper.isWritableProperty(propertyName)) {
                wrapper.setPropertyValue(propertyName, value);
            } else {
                fields.setPropertyValue(propertyName, value);
            }

            touchedProperties.add(propertyName);
            generatedProperties.remove(propertyName);
            return this;
        }

        @Override
        public ConfigurableBuildCommand<T> withGeneratedValue(String propertyName, ValueGenerator generator) {
            PropertyDescriptor descriptor = wrapper.getPropertyDescriptor(propertyName);
            Object value = generator.generate(descriptor.getPropertyType());
            return this.withValue(propertyName, value);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ConfigurableBuildCommand<T> withGeneratedValue(String propertyName) {
            touchedProperties.add(propertyName);
            generatedProperties.add(propertyName);
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ConfigurableBuildCommand<T> fill() {
            for (PropertyDescriptor descriptor : wrapper.getPropertyDescriptors()) {
                String propertyName = descriptor.getName();
                if (wrapper.isWritableProperty(propertyName) && ! touchedProperties.contains(propertyName)) {
                    if (! builder.skippedProperties.contains(new PropertyReference(descriptor))) {
                        withGeneratedValue(propertyName);
                    }
                }
            }
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public T build() {
            return build(false);
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public T buildAndSave() {
            T bean = build(true);
            return builder.saver.save(bean);
        }
        
        @SuppressWarnings("unchecked")
        private T build(boolean autoSave) {
            for (String name : new HashSet<>(generatedProperties)) {
                PropertyDescriptor descriptor = wrapper.getPropertyDescriptor(name);
                Object value = builder.generateValue(wrapper.getWrappedClass(), descriptor);
                if (autoSave) {
                    value = builder.saver.save(value);
                }
                withValue(name, value);
            }
            return (T) wrapper.getWrappedInstance();
        }

    }

}
