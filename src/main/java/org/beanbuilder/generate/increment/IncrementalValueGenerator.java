/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package org.beanbuilder.generate.increment;

import org.beanbuilder.generate.ValueGenerator;

/**
 * Value generator capable of incrementing itself. 
 *
 * @author Jeroen van Schagen
 * @since Apr 11, 2014
 */
public abstract class IncrementalValueGenerator<T> implements ValueGenerator {
    
    private T current;
    
    public IncrementalValueGenerator(T initial) {
        this.current = initial;
    }
    
    @Override
    public Object generate(Class<?> valueType) {
        T result = current;
        current = increment(current);
        return result;
    }
    
    protected abstract T increment(T current);

}
