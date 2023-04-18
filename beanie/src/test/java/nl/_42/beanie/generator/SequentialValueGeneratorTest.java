/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.beanie.generator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
        Assertions.assertEquals(1, generator.generate(null));
        Assertions.assertEquals(2, generator.generate(null));
        Assertions.assertEquals(3, generator.generate(null));
        Assertions.assertNull(generator.generate(null));
    }
    
    @Test
    public void testSequenceRepeat() {
        SequentialValueGenerator generator = new SequentialValueGenerator(new Integer[] { 1, 2, 3 }).repeatable();
        Assertions.assertEquals(1, generator.generate(null));
        Assertions.assertEquals(2, generator.generate(null));
        Assertions.assertEquals(3, generator.generate(null));
        Assertions.assertEquals(1, generator.generate(null));
    }

}
