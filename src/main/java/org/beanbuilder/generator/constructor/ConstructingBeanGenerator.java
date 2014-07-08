/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package org.beanbuilder.generator.constructor;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Set;

import org.beanbuilder.generator.ValueGenerator;
import org.beanbuilder.support.Classes;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

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
        if (Classes.isNotConcrete(beanClass)) {
            beanClass = getSomeImplementationClass(beanClass);
        }

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

    private static Class<?> getSomeImplementationClass(Class<?> beanClass) {
        String implementationName = getSomeImplementationName(beanClass);
        return Classes.forName(implementationName);
    }
    
    private static String getSomeImplementationName(final Class<?> beanClass) {
        ClassPathScanningCandidateComponentProvider implementationsProvider = new ClassPathScanningCandidateComponentProvider(false);
        implementationsProvider.addIncludeFilter(new AssignableTypeFilter(beanClass));
        implementationsProvider.addExcludeFilter(new IsNotConcreteFilter());

        Set<BeanDefinition> implementations = implementationsProvider.findCandidateComponents(beanClass.getPackage().getName());
        if (implementations.isEmpty()) {
            throw new IllegalStateException("Could not find an implementation class of " + beanClass.getName() + " in (sub)package.");
        }
        return implementations.iterator().next().getBeanClassName();
    }
    
    private static class IsNotConcreteFilter implements TypeFilter {
        
        @Override
        public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
            return !metadataReader.getClassMetadata().isConcrete();
        }
        
    }

}
