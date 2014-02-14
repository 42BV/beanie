/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package org.beanbuilder.generate.construction;

import java.lang.reflect.Constructor;

import org.beanbuilder.generate.ValueGenerator;
import org.springframework.beans.BeanUtils;

/**
 * Generator that constructs a bean. 
 *
 * @author jeroen
 * @since Feb 14, 2014
 */
public class ConstructingBeanGenerator implements ValueGenerator {
    
    private final ConstructorStrategy constructorStrategy;

    private final ValueGenerator argumentsGenerator;

    public ConstructingBeanGenerator(ConstructorStrategy constructorStrategy, ValueGenerator argumentsGenerator) {
        this.constructorStrategy = constructorStrategy;
        this.argumentsGenerator = argumentsGenerator;
    }

    @Override
    public Object generate(Class<?> beanClass) {
        // TODO: What if abstract

        Constructor<?> constructor = constructorStrategy.getConstructor(beanClass);
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

}
