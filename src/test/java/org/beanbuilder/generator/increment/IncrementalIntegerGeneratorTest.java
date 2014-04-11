/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package org.beanbuilder.generator.increment;

import org.beanbuilder.generator.increment.IncrementalIntegerGenerator;
import org.junit.Assert;
import org.junit.Test;

/**
 * 
 *
 * @author Jeroen van Schagen
 * @since Apr 11, 2014
 */
public class IncrementalIntegerGeneratorTest {
    
    @Test
    public void testSequence() {
        IncrementalIntegerGenerator generator = new IncrementalIntegerGenerator();
        Assert.assertEquals(1, generator.generate(null));
        Assert.assertEquals(2, generator.generate(null));
        Assert.assertEquals(3, generator.generate(null));
    }

}
