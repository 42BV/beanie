/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.beanie.generator.increment;

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
