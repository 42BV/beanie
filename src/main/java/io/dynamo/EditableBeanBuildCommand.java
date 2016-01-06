/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.dynamo;

import io.dynamo.generator.ValueGenerator;

/**
 * Bean build command that allows users to declare custom property values.
 *
 * @author Jeroen van Schagen
 * @since Feb 14, 2014
 */
public interface EditableBeanBuildCommand<T> extends BeanBuildCommand<T> {

    /**
     * Declare a value in our to be generated bean.
     * 
     * @param propertyName the property name
     * @param value the property value
     * @return this instance, for chaining
     */
    EditableBeanBuildCommand<T> withValue(String propertyName, Object value);
    
    /**
     * Copies all usable property values from a bean
     * into our result.
     * 
     * @param bean the bean to copy properties from
     * @param exclusions the property names to exclude from copy
     * @return this instance, for chaining
     */
    EditableBeanBuildCommand<T> load(Object bean, String... exclusions);

    /**
     * Generate a value in our to be generated bean.
     * 
     * @param propertyName the property name
     * @param generator the value generator
     * @return this instance, for chaining
     */
    EditableBeanBuildCommand<T> generateValue(String propertyName, ValueGenerator generator);
    
    /**
     * Generate a value in our to be generated bean.
     * 
     * @param propertyNames the property names
     * @return this instance, for chaining
     */
    EditableBeanBuildCommand<T> generateValue(String... propertyNames);

}