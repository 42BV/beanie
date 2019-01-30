/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.beanie;

import nl._42.beanie.generator.ValueGenerator;

import java.util.function.Consumer;
import java.util.function.Function;

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
     * Perform a mapping on the intermediate object, changing it
     * into the result object of our function.
     * @param function the function that should be performed
     * @return this instance, for chaining
     */
    EditableBeanBuildCommand<T> map(Function<T, T> function);

    /**
     * Perform a mapping on the intermediate object, changing the
     * type to targetType.
     * @param targetType The type the intermediate object should continue to have
     * @param <M> The target class
     * @return new instance of EditableBeanBuildCommand
     */
    <M> EditableBeanBuildCommand<M> map(Class<M> targetType);

    /**
     * Combination of as and map, performs a mapping on the intermediate object,
     * changing the type to the targetType and changing the EditableBeanBuild command interface.
     * @param interfaceType The new interface type to continue with
     * @param targetType The type the intermediate object should continue to have
     * @param <I> The new EditableBeanBuildCommand interface
     * @param <M> The target class
     * @return new instance of EditableBeanBuildCommand

     */
    <I extends EditableBeanBuildCommand<M>, M> I map(Class<I> interfaceType, Class<M> targetType);

    /**
     * Changes the interface type. This allows you to continue with another command.
     *
     * @param <I> the interface type
     * @param <T> the bean type
     * @param interfaceType the new interface type class
     * @return new command instance of specified type
     */
    <I extends EditableBeanBuildCommand<T>, T> I as(Class<I> interfaceType);

    /**
     * Perform an operation on the intermediate object.
     * @param consumer the consumer that should take our object
     * @return this instance, for chaining
     */
    EditableBeanBuildCommand<T> doWith(Consumer<T> consumer);

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