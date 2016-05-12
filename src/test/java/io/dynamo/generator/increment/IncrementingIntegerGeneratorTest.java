/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package io.dynamo.generator.increment;

import io.dynamo.generator.increment.IntegerValueIncrementor;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 *
 * @author Jeroen van Schagen
 * @since Apr 11, 2014
 */
public class IncrementingIntegerGeneratorTest {
    
    @Test
    public void testSequence() {
        IntegerValueIncrementor generator = new IntegerValueIncrementor();
        Assert.assertEquals(1, generator.generate(null));
        Assert.assertEquals(2, generator.generate(null));
        Assert.assertEquals(3, generator.generate(null));
    }

}
