/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package io.dynamo.generator;

import io.dynamo.generator.constructor.ConstructorStrategy;
import io.dynamo.generator.constructor.ShortestConstructorStrategy;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.springframework.beans.BeanUtils;

/**
 * Generator that constructs a bean. 
 *
 * @author Jeroen van Schagen
 * @since Feb 14, 2014
 */
public class BeanGenerator implements ValueGenerator {
    
    /**
     * Generates argument value instances.
     */
    private final ValueGenerator constructorArgGenerator;

    /**
     * Selects the most desired constructor.
     */
    private ConstructorStrategy constructorStrategy = new ShortestConstructorStrategy();

    /**
     * Generates abstract class instances.
     */
    private ValueGenerator abstractGenerator = new ProxyBeanGenerator();
    
    /**
     * Generates interface instances.
     */
    private ValueGenerator interfaceGenerator = new ProxyBeanGenerator();
    
    /**
     * Construct a new {@link BeanGenerator}.
     * 
     * @param constructorArgGenerator generator used to generate constructor arguments
     */
    public BeanGenerator(ValueGenerator constructorArgGenerator) {
        this.constructorArgGenerator = constructorArgGenerator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object generate(Class<?> beanClass) {
        if (beanClass.isInterface()) {
            return interfaceGenerator.generate(beanClass);
        } else if (Modifier.isAbstract(beanClass.getModifiers())) {
            return abstractGenerator.generate(beanClass);
        } else {
            return instantiate(beanClass);
        }
    }

    private Object instantiate(Class<?> beanClass) {
        Constructor<?> constructor = constructorStrategy.findConstructor(beanClass);
        if (constructor != null) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            Object[] arguments = new Object[parameterTypes.length];
            for (int index = 0; index < parameterTypes.length; index++) {
                arguments[index] = constructorArgGenerator.generate(parameterTypes[index]);
            }
            return BeanUtils.instantiateClass(constructor, arguments);
        } else {
            return BeanUtils.instantiateClass(beanClass);
        }
    }

    /**
     * Change the generator used to generate abstract class instances.
     * 
     * @param abstractGenerator the abstract generator
     * @return this instance for chaining
     */
    public BeanGenerator setAbstractGenerator(ValueGenerator abstractGenerator) {
        this.abstractGenerator = abstractGenerator;
        return this;
    }

    /**
     * Change the generator used to generate interface instances.
     * 
     * @param interfaceGenerator the interface generator
     * @return this instance for chaining
     */
    public BeanGenerator setInterfaceGenerator(ValueGenerator interfaceGenerator) {
        this.interfaceGenerator = interfaceGenerator;
        return this;
    }

    /**
     * Change the strategory for selecting the most desired constructor.
     * 
     * @param constructorStrategy the constructor strategy
     * @return this instance for chaining
     */
    public BeanGenerator setConstructorStrategy(ConstructorStrategy constructorStrategy) {
        this.constructorStrategy = constructorStrategy;
        return this;
    }

}
