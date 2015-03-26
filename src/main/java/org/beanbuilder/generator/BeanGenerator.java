/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package org.beanbuilder.generator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.beanbuilder.generator.constructor.ConstructorStrategy;
import org.springframework.beans.BeanUtils;

/**
 * Generator that constructs a bean. 
 *
 * @author Jeroen van Schagen
 * @since Feb 14, 2014
 */
public class BeanGenerator implements ValueGenerator {
    
    private final ConstructorStrategy constructorStrategy;

    private final ValueGenerator constructorArgGenerator;

    private final ValueGenerator abstractGenerator = new FirstImplBeanGenerator(this);
    
    private final ValueGenerator interfaceGenerator = new InterfaceProxyBeanGenerator();
    
    public BeanGenerator(ConstructorStrategy constructorStrategy, ValueGenerator constructorArgGenerator) {
        this.constructorStrategy = constructorStrategy;
        this.constructorArgGenerator = constructorArgGenerator;
    }

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

}
