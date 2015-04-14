/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package org.beanbuilder;

import org.beanbuilder.generator.ValueGenerator;

/**
 * Bean build command that allows users to declare custom property values.
 *
 * @author Jeroen van Schagen
 * @since Feb 14, 2014
 */
public interface EditableBeanBuildCommand<T> extends BeanBuildCommand<T> {

    /**
     * Generate a value in our to be generated bean.
     * 
     * @param propertyName the property name
     * @return this instance, for chaining
     */
    EditableBeanBuildCommand<T> generateValue(String propertyName);
    
    /**
     * Generate a value in our to be generated bean.
     * 
     * @param propertyName the property name
     * @param generator the value generator
     * @return this instance, for chaining
     */
    EditableBeanBuildCommand<T> generateValue(String propertyName, ValueGenerator generator);
    
    /**
     * Declare a value in our to be generated bean.
     * 
     * @param propertyName the property name
     * @param value the property value
     * @return this instance, for chaining
     */
    EditableBeanBuildCommand<T> setValue(String propertyName, Object value);
    
    /**
     * Copies all usable property values from a bean
     * into our result.
     * 
     * @param bean the bean to copy properties from
     * @param exclusions the property names to exclude from copy
     * @return this instance, for chaining
     */
    EditableBeanBuildCommand<T> copyAllValuesFrom(Object bean, String... exclusions);

}