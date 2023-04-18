/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.beanie.generator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DefaultConfigurableValueGeneratorTest {
    
    @Test
    public void testUnknownType() {
        Assertions.assertThrows(IllegalArgumentException.class, () ->
            new DefaultValueGenerator(null).generate(Test.class)
        );
    }

}
