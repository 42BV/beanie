/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package org.beanbuilder;

import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;

import org.beanbuilder.generate.ConfigurableValueGenerator;
import org.beanbuilder.generate.ConstantValueGenerator;
import org.beanbuilder.generate.ValueGenerator;
import org.beanbuilder.generate.construction.ConstructingBeanGenerator;
import org.beanbuilder.generate.construction.ConstructorStrategy;
import org.beanbuilder.generate.construction.ShortestConstructorStrategy;
import org.beanbuilder.support.PropertyReference;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;


/**
 * Builds new bean instances.
 *
 * @author Jeroen van Schagen
 * @since Feb 14, 2014
 */
public class BeanBuilder implements ValueGenerator {
    
    private final Map<PropertyReference, ValueGenerator> propertyValueGenerators;
    
    private final ConfigurableValueGenerator typeValueGenerator;
    
    private final ConstructingBeanGenerator constructingGenerator;

    public BeanBuilder() {
        this(new ShortestConstructorStrategy());
    }
    
    public BeanBuilder(ConstructorStrategy constructorStrategy) {
        propertyValueGenerators = new HashMap<PropertyReference, ValueGenerator>();
        typeValueGenerator = new ConfigurableValueGenerator(this);
        constructingGenerator = new ConstructingBeanGenerator(constructorStrategy, typeValueGenerator);
    }

    /**
     * Start building a new bean.
     * 
     * @param beanClass the type of bean to start building
     * @return the bean build command
     */
    public <T> BeanBuildCommand<T> start(Class<T> beanClass) {
        return new BeanBuildCommand<T>(beanClass);
    }
    
    public class BeanBuildCommand<T> {
        
        private final Class<T> beanClass;

        private BeanWrapper beanWrapper;

        public BeanBuildCommand(Class<T> beanClass) {
            this.beanClass = beanClass;

            Object bean = constructingGenerator.generate(beanClass);
            beanWrapper = new BeanWrapperImpl(bean);
        }
        
        public BeanBuildCommand<T> withValue(String propertyName, Object value) {
            beanWrapper.setPropertyValue(propertyName, value);
            return this;
        }

        public BeanBuildCommand<T> withGeneratedValue(String propertyName) {
            PropertyDescriptor propertyDescriptor = beanWrapper.getPropertyDescriptor(propertyName);
            Object value = generatePropertyValue(propertyDescriptor);
            return this.withValue(propertyName, value);
        }
        
        private Object generatePropertyValue(PropertyDescriptor propertyDescriptor) {
            ValueGenerator generator = getSupportedGenerator(propertyDescriptor.getName());
            return generator.generate(propertyDescriptor.getPropertyType());
        }
        
        private ValueGenerator getSupportedGenerator(String propertyName) {
            PropertyReference propertyReference = new PropertyReference(beanClass, propertyName);
            if (propertyValueGenerators.containsKey(propertyReference)) {
                return propertyValueGenerators.get(propertyReference);
            } else {
                return typeValueGenerator;
            }
        }

        public BeanBuildCommand<T> withGeneratedValues() {
            for (PropertyDescriptor propertyDescriptor : beanWrapper.getPropertyDescriptors()) {
                if (propertyDescriptor.getWriteMethod() != null) {
                    Object value = generatePropertyValue(propertyDescriptor);
                    withValue(propertyDescriptor.getName(), value);
                }
            }
            return this;
        }

        @SuppressWarnings("unchecked")
        public T build() {
            return (T) beanWrapper.getWrappedInstance();
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object generate(Class<?> beanClass) {
        if (typeValueGenerator.contains(beanClass)) {
            return typeValueGenerator.generate(beanClass);
        } else {
            return start(beanClass).withGeneratedValues().build();
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

}
