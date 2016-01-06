/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package io.dynamo.generator.random;

import io.dynamo.generator.ValueGenerator;


/**
 * Generates a random integer between the specified minimum and maximum range.
 *
 * @author Sander Benschop
 * @since Feb 14, 2014
 */
public class RandomIntegerGenerator extends RandomSupport implements ValueGenerator {

    private final int minimum;
    
    private final int maximum;
    
    public RandomIntegerGenerator(int minimum, int maximum) {
        this.minimum = Math.max(minimum, 0);
        this.maximum = Math.max(maximum, 0);
    }
    
    @Override
    public Integer generate(Class<?> type) {
        return minimum + randomInt(maximum - minimum);
    }

}
