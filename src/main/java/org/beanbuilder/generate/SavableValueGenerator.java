/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package org.beanbuilder.generate;

/**
 * Generates values that can be saved.
 *
 * @author Jeroen van Schagen
 * @since Feb 14, 2014
 */
public interface SavableValueGenerator extends ValueGenerator {
    
    /**
     * Generate a new value and saves it automatically.
     * 
     * @param valueType the value type
     * @return the generated, and saved, value
     */
    Object generateAndSave(Class<?> valueType);

}
