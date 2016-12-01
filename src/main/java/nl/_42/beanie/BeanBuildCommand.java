/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.beanie;

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