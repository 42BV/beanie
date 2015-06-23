/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package io.beanbuilder.generator;

import io.beanbuilder.generator.SequentialValueGenerator;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 *
 * @author Jeroen van Schagen
 * @since Apr 11, 2014
 */
public class SequentialValueGeneratorTest {
    
    @Test
    public void testSequence() {
        SequentialValueGenerator generator = new SequentialValueGenerator(new Integer[] { 1, 2, 3 });
        Assert.assertEquals(1, generator.generate(null));
        Assert.assertEquals(2, generator.generate(null));
        Assert.assertEquals(3, generator.generate(null));
        Assert.assertNull(generator.generate(null));
    }
    
    @Test
    public void testSequenceRepeat() {
        SequentialValueGenerator generator = new SequentialValueGenerator(new Integer[] { 1, 2, 3 }).repeatable();
        Assert.assertEquals(1, generator.generate(null));
        Assert.assertEquals(2, generator.generate(null));
        Assert.assertEquals(3, generator.generate(null));
        Assert.assertEquals(1, generator.generate(null));
    }

}
