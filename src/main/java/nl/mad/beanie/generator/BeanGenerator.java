/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package nl.mad.beanie.generator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import nl.mad.beanie.generator.constructor.ConstructorStrategy;
import nl.mad.beanie.generator.constructor.ShortestConstructorStrategy;

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
            return instantiateConcrete(beanClass);
        }
    }

    // Instantiate the concrete class.
    private Object instantiateConcrete(Class<?> beanClass) {
        Constructor<?> constructor = constructorStrategy.getConstructor(beanClass);
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
     */
    public void setAbstractGenerator(ValueGenerator abstractGenerator) {
        this.abstractGenerator = abstractGenerator;
    }

    /**
     * Change the generator used to generate interface instances.
     * 
     * @param interfaceGenerator the interface generator
     */
    public void setInterfaceGenerator(ValueGenerator interfaceGenerator) {
        this.interfaceGenerator = interfaceGenerator;
    }

    /**
     * Change the strategory for selecting the most desired constructor.
     * 
     * @param constructorStrategy the constructor strategy
     */
    public void setConstructorStrategy(ConstructorStrategy constructorStrategy) {
        this.constructorStrategy = constructorStrategy;
    }

}
