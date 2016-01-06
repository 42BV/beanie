/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.dynamo.generator;

import io.dynamo.util.Classes;

import java.io.IOException;
import java.util.Set;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

/**
 * Generator that finds the first implementation of a class
 * and then generates that class.
 *
 * @author Jeroen van Schagen
 * @since Mar 26, 2015
 */
public class FirstImplBeanGenerator implements ValueGenerator {
    
    private final ValueGenerator beanGenerator;
    
    public FirstImplBeanGenerator(ValueGenerator beanGenerator) {
        this.beanGenerator = beanGenerator;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object generate(Class<?> abstractBeanClass) {
        Class<?> implementationClass = getFirstImplementationClass(abstractBeanClass);
        return beanGenerator.generate(implementationClass);
    }
    
    private static Class<?> getFirstImplementationClass(final Class<?> beanClass) {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(beanClass));
        provider.addExcludeFilter(new IsNotConcreteFilter());
        
        Set<BeanDefinition> implementations = provider.findCandidateComponents(beanClass.getPackage().getName());
        if (implementations.isEmpty()) {
            throw new IllegalStateException("Could not find an implementation class of " + beanClass.getName() + " in (sub)package.");
        }
        
        String className = implementations.iterator().next().getBeanClassName();
        return Classes.forName(className);
    }
    
    private static class IsNotConcreteFilter implements TypeFilter {
        
        @Override
        public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
            return !metadataReader.getClassMetadata().isConcrete();
        }
        
    }
    
}
