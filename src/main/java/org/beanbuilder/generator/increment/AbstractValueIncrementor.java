/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package org.beanbuilder.generator.increment;

import org.beanbuilder.generator.ValueGenerator;

/**
 * Value generator capable of incrementing itself. 
 *
 * @author Jeroen van Schagen
 * @since Apr 11, 2014
 */
public abstract class AbstractValueIncrementor<T> implements ValueGenerator {
    
    private T current;
    
    public AbstractValueIncrementor(T initial) {
        this.current = initial;
    }
    
    @Override
    public Object generate(Class<?> type) {
        T result = current;
        current = increment(current);
        return result;
    }
    
    protected abstract T increment(T current);

}
