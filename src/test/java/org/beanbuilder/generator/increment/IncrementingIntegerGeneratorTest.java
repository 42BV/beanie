/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package org.beanbuilder.generator.increment;

import org.beanbuilder.generator.increment.IncrementingIntegerGenerator;
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
        IncrementingIntegerGenerator generator = new IncrementingIntegerGenerator();
        Assert.assertEquals(1, generator.generate(null));
        Assert.assertEquals(2, generator.generate(null));
        Assert.assertEquals(3, generator.generate(null));
    }

}
