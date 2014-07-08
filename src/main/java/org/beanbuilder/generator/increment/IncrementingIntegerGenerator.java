/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package org.beanbuilder.generator.increment;

/**
 * Generates a larger integer value during each invocation.
 *
 * @author Jeroen van Schagen
 * @since Apr 11, 2014
 */
public class IncrementingIntegerGenerator extends IncrementingValueGenerator<Integer> {

    public IncrementingIntegerGenerator() {
        this(Integer.valueOf(1));
    }

    public IncrementingIntegerGenerator(Integer initial) {
        super(initial);
    }
    
    @Override
    protected Integer increment(Integer current) {
        return Integer.valueOf(current + 1);
    }
    
}
