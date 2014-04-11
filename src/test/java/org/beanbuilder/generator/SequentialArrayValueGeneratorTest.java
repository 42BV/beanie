/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package org.beanbuilder.generator;

import org.beanbuilder.generate.SequentialArrayValueGenerator;
import org.junit.Assert;
import org.junit.Test;

/**
 * 
 *
 * @author Jeroen van Schagen
 * @since Apr 11, 2014
 */
public class SequentialArrayValueGeneratorTest {
    
    @Test
    public void testSequence() {
        SequentialArrayValueGenerator generator = new SequentialArrayValueGenerator(1, 2, 3);
        Assert.assertEquals(1, generator.generate(null));
        Assert.assertEquals(2, generator.generate(null));
        Assert.assertEquals(3, generator.generate(null));
        Assert.assertNull(generator.generate(null));
    }
    
    @Test
    public void testSequenceWithReset() {
        SequentialArrayValueGenerator generator = new SequentialArrayValueGenerator(1, 2, 3).resetWhenFinished();
        Assert.assertEquals(1, generator.generate(null));
        Assert.assertEquals(2, generator.generate(null));
        Assert.assertEquals(3, generator.generate(null));
        Assert.assertEquals(1, generator.generate(null));
    }

}
