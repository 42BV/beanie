/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.beanie;

import java.util.function.Consumer;
import java.util.function.Function;

import nl._42.beanie.generator.ValueGenerator;

/**
 * Command used to construct and save beans.
 * 
 * @see BeanBuildCommand#construct()
 * @see BeanBuildCommand#save()
 *
 * @author Jeroen van Schagen
 * @since Feb 14, 2014
 */
public interface BeanBuildCommand<T> {

    /**
     * Declare a value in our to be generated bean.
     * 
     * @param propertyName the property name
     * @param value the property value
     * @return this instance, for chaining
     */
    BeanBuildCommand<T> withValue(String propertyName, Object value);
    
    /**
     * Copies all usable property values from a bean
     * into our result.
     * 
     * @param bean the bean to copy properties from
     * @param exclusions the property names to exclude from copy
     * @return this instance, for chaining
     */
    BeanBuildCommand<T> load(Object bean, String... exclusions);
    
    /**
     * Changes the interface type.
     * This allows you to continue with another EditableBeanBuildCommand.
     * @param interfaceType The type the intermediate object should continue to have
     * @param <C> target interface
     * @param <R> target type
     * @return new instance of EditableBeanBuildCommand
     */
    <C extends BeanBuildCommand<R>, R> C as(Class<C> interfaceType);

    /**
     * Perform a mapping on the intermediate object, changing it
     * into the result object of our function.
     * @param function the function that should be performed
     * @param <R> target type
     * @return this instance, for chaining
     */
    <R> BeanBuildCommand<R> map(Function<T, R> function);

    /**
     * Perform a mapping on the intermediate object, changing the type to target type.
     * @param targetType The type the intermediate object should continue to have
     * @param <R> target type
     * @return new instance of bean build command
     */
    <R> BeanBuildCommand<R> map(Class<R> targetType);

    /**
     * Combination of as and map, performs a mapping on the intermediate object,
     * changing the type to the targetType and changing the bean build command interface.
     * @param interfaceType The new interface type to continue with
     * @param targetType The type the intermediate object should continue to have
     * @param <C> target interface
     * @param <R> target type
     * @return new instance of EditableBeanBuildCommand
     */
    <C extends BeanBuildCommand<R>, R> C map(Class<C> interfaceType, Class<R> targetType);

    /**
     * Perform an operation on the intermediate object.
     * @param consumer the consumer that should take our object
     * @return this instance, for chaining
     */
    BeanBuildCommand<T> perform(Consumer<T> consumer);

    /**
     * Generate a value in our to be generated bean.
     * 
     * @param propertyName the property name
     * @param generator the value generator
     * @return this instance, for chaining
     */
    BeanBuildCommand<T> generateValue(String propertyName, ValueGenerator generator);
    
    /**
     * Generate a value in our to be generated bean.
     * 
     * @param propertyNames the property names
     * @return this instance, for chaining
     */
    BeanBuildCommand<T> generateValue(String... propertyNames);
    
    /**
     * Generate all untouched, changable, values in our bean.
     * 
     * @return this instance, for chaining
     */
    BeanBuildCommand<T> fill();
    
    /**
     * Construct the new bean.
     * 
     * @return the constructed bean
     */
    T construct();
    
    /**
     * Construct the new bean.
     * 
     * @param autoSave whether to auto save
     * @return the constructed instance
     */
    T construct(boolean autoSave);
    
    /**
     * Construct and save new bean.
     * 
     * @return the saved bean
     */
    T save();

}