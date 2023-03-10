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
public class UUIDStringGeneratorTest {
    
    @Test
    public void testGenerate() {
        UUIDStringGenerator generator = new UUIDStringGenerator();
        String first = generator.generate(null);
        Assertions.assertNotNull(first);
        String second = generator.generate(null);
        Assertions.assertNotNull(first);
        Assertions.assertNotEquals(first, second);
    }

}
