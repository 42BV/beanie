/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package io.beanbuilder.generator.increment;

/**
 * Generates a larger integer value during each invocation.
 *
 * @author Jeroen van Schagen
 * @since Apr 11, 2014
 */
public class IntegerValueIncrementor extends AbstractValueIncrementor<Integer> {

    public IntegerValueIncrementor() {
        this(Integer.valueOf(1));
    }

    public IntegerValueIncrementor(Integer initial) {
        super(initial);
    }
    
    @Override
    protected Integer increment(Integer current) {
        return Integer.valueOf(current + 1);
    }
    
}
