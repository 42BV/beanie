/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package org.beanbuilder;

import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;

import org.beanbuilder.generate.ConstantValueGenerator;
import org.beanbuilder.generate.TypeValueGenerator;
import org.beanbuilder.generate.ValueGenerator;
import org.beanbuilder.generate.construction.ConstructingBeanGenerator;
import org.beanbuilder.generate.construction.ConstructorStrategy;
import org.beanbuilder.generate.construction.ShortestConstructorStrategy;
import org.beanbuilder.save.BeanSaver;
import org.beanbuilder.save.UnsupportedBeanSaver;
import org.beanbuilder.support.PropertyReference;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.target.SingletonTargetSource;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.core.GenericTypeResolver;


/**
 * Builds new bean instances.
 *
 * @author Jeroen van Schagen
 * @since Feb 14, 2014
 */
public class BeanBuilder implements ValueGenerator {
    
    private final Map<PropertyReference, ValueGenerator> propertyValueGenerators = new HashMap<>();
    
    private final TypeValueGenerator typeValueGenerator;
    
    private final ConstructingBeanGenerator beanGenerator;
    
    private final BeanSaver beanSaver;

    public BeanBuilder() {
        this(new ShortestConstructorStrategy());
    }
    
    public BeanBuilder(ConstructorStrategy constructorStrategy) {
        this(new ShortestConstructorStrategy(), new UnsupportedBeanSaver());
    }
    
    public BeanBuilder(ConstructorStrategy constructorStrategy, BeanSaver beanSaver) {
        this.typeValueGenerator = new TypeValueGenerator(this);
        
        this.beanGenerator = new ConstructingBeanGenerator(constructorStrategy, this);
        this.beanSaver = beanSaver;
    }

    /**
     * Start building a new bean.
     * 
     * @param beanClass the type of bean to start building
     * @return the bean build command
     */
    public <T> ConfigurableBeanBuildCommand<T> newBean(Class<T> beanClass) {
        return new DefaultBeanBuildCommand<T>(this, beanClass);
    }
    
    /**
     * Start building a new bean, using a custom builder interface.
     * 
     * @param beanClass the type of bean to start building
     * @param commandType the build command interface
     * @return the builder instance, capable of building beans
     */
    @SuppressWarnings("unchecked")
    public <T extends BeanBuildCommand<?>> T newBeanBy(Class<T> commandType) {
        final Class<?> beanClass = GenericTypeResolver.resolveTypeArguments(commandType, BeanBuildCommand.class)[0];
        final ConfigurableBeanBuildCommand<?> command = newBean(beanClass);

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
        if (typeValueGenerator.contains(beanClass)) {
            return typeValueGenerator.generate(beanClass);
        } else {
            return newBean(beanClass).generateValues().build();
        }
    }
    
    private Object generatePropertyValue(Class<?> beanClass, PropertyDescriptor propertyDescriptor) {
        ValueGenerator generator = getPropertyGenerator(beanClass, propertyDescriptor.getName());
        return generator.generate(propertyDescriptor.getPropertyType());
    }
    
    private ValueGenerator getPropertyGenerator(Class<?> beanClass, String propertyName) {
        PropertyReference propertyReference = new PropertyReference(beanClass, propertyName);
        if (propertyValueGenerators.containsKey(propertyReference)) {
            return propertyValueGenerators.get(propertyReference);
        } else {
            return typeValueGenerator;
        }
    }
    
    /**
    * Register a value generation strategy for a specific property.
    * 
    * @param declaringClass the bean class that declares our property
    * @param propertyName the name of the property
    * @param generator the generation strategy
    * @return this instance
    */
    public BeanBuilder register(Class<?> declaringClass, String propertyName, ValueGenerator generator) {
        propertyValueGenerators.put(new PropertyReference(declaringClass, propertyName), generator);
        return this;
    }
    
    /**
    * Register a constant value for a specific property.
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
        typeValueGenerator.register(valueType, generator);
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
    public interface BeanBuildCommand<T> {
        
        /**
         * Generate all value in our to be generated bean.
         * 
         * @return this instance, for chaining
         */
        BeanBuildCommand<T> generateValues();
        
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
    public interface ConfigurableBeanBuildCommand<T> extends BeanBuildCommand<T> {
        
        /**
         * Generate a value in our to be generated bean.
         * 
         * @param propertyName the property name
         * @return this instance, for chaining
         */
        ConfigurableBeanBuildCommand<T> generateValue(String propertyName);
        
        /**
         * Generate all value in our to be generated bean.
         * 
         * @return this instance, for chaining
         */
        @Override
        ConfigurableBeanBuildCommand<T> generateValues();
        
        /**
         * Declare a value in our to be generated bean.
         * 
         * @param propertyName the property name
         * @param value the property value
         * @return this instance, for chaining
         */
        ConfigurableBeanBuildCommand<T> withValue(String propertyName, Object value);

    }

    /**
     * Default implementation of the bean build command.
     *
     * @author Jeroen van Schagen
     * @since Feb 14, 2014
     */
    private static class DefaultBeanBuildCommand<T> implements ConfigurableBeanBuildCommand<T> {
        
        private final BeanBuilder beanBuilder;
        
        private final Class<T> beanClass;

        private final BeanWrapper beanWrapper;

        public DefaultBeanBuildCommand(BeanBuilder beanBuilder, Class<T> beanClass) {
            this.beanBuilder = beanBuilder;
            this.beanClass = beanClass;

            Object bean = beanBuilder.beanGenerator.generate(beanClass);
            beanWrapper = new BeanWrapperImpl(bean);
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public ConfigurableBeanBuildCommand<T> withValue(String propertyName, Object value) {
            beanWrapper.setPropertyValue(propertyName, value);
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ConfigurableBeanBuildCommand<T> generateValue(String propertyName) {
            PropertyDescriptor propertyDescriptor = beanWrapper.getPropertyDescriptor(propertyName);
            Object value = beanBuilder.generatePropertyValue(beanClass, propertyDescriptor);
            return this.withValue(propertyName, value);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ConfigurableBeanBuildCommand<T> generateValues() {
            for (PropertyDescriptor propertyDescriptor : beanWrapper.getPropertyDescriptors()) {
                if (propertyDescriptor.getWriteMethod() != null) {
                    Object value = beanBuilder.generatePropertyValue(beanClass, propertyDescriptor);
                    withValue(propertyDescriptor.getName(), value);
                }
            }
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @SuppressWarnings("unchecked")
        public T build() {
            return (T) beanWrapper.getWrappedInstance();
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public T buildAndSave() {
            T bean = build();
            return beanBuilder.beanSaver.save(bean);
        }

    }

}
