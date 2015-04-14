/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package org.beanbuilder;

/**
 * Command for building beans.
 *
 * @author Jeroen van Schagen
 * @since Feb 14, 2014
 */
public interface BeanBuildCommand<T> {
    
    /**
     * Generate all untouched, changable, values in our bean.
     * 
     * @return this instance, for chaining
     */
    BeanBuildCommand<T> fill();
    
    /**
     * Build the new bean.
     * 
     * @return the created bean
     */
    T build();
    
    /**
     * Build and save new bean.
     * 
     * @return the saved bean
     */
    T save();

}