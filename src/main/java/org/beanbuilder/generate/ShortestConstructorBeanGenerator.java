/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package org.beanbuilder.generate;

import java.lang.reflect.Constructor;

import org.springframework.beans.BeanUtils;

/**
 * Generator that constructs a bean. 
 *
 * @author jeroen
 * @since Feb 14, 2014
 */
public class ShortestConstructorBeanGenerator implements ValueGenerator {
    
    private final ValueGenerator argumentsGenerator;

    public ShortestConstructorBeanGenerator(ValueGenerator argumentsGenerator) {
        this.argumentsGenerator = argumentsGenerator;
    }

    @Override
    public Object generate(Class<?> beanClass) {
        Constructor<?> constructor = getShortestConstructor(beanClass);
        if (constructor != null) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            Object[] arguments = new Object[parameterTypes.length];
            for (int index = 0; index < parameterTypes.length; index++) {
                arguments[index] = argumentsGenerator.generate(parameterTypes[index]);
            }
            return BeanUtils.instantiateClass(constructor, arguments);
        } else {
            return BeanUtils.instantiateClass(beanClass);
        }
    }
    
    private Constructor<?> getShortestConstructor(Class<?> beanClass) {
        Constructor<?> shortest = null;
        for (Constructor<?> constructor : beanClass.getDeclaredConstructors()) {
            if (shortest == null || shortest.getParameterTypes().length > constructor.getParameterTypes().length) {
                shortest = constructor;
            }
        }
        return shortest;
    }

}
