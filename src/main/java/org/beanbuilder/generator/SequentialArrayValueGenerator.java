/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package org.beanbuilder.generator;

/**
 * Returns the values in sequence.
 *
 * @author Jeroen van Schagen
 * @since Apr 11, 2014
 */
public class SequentialArrayValueGenerator implements ValueGenerator {
    
    private final Object[] values;
    
    private boolean resetWhenFinished = false;

    private int index = 0;
    
    public SequentialArrayValueGenerator(Object[] values) {
        this.values = values;
    }

    public SequentialArrayValueGenerator resetWhenFinished() {
        resetWhenFinished = true;
        return this;
    }

    @Override
    public Object generate(Class<?> valueType) {
        if (index >= values.length) {
            if (resetWhenFinished) {
                reset();
            } else {
                return null;
            }
        }
        return values[index++];
    }

    public void reset() {
        index = 0;
    }

}
