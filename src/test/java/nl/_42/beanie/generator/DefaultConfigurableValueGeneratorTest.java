/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.beanie.generator;

import org.junit.Test;

public class DefaultConfigurableValueGeneratorTest {
    
    @Test(expected = IllegalArgumentException.class)
    public void testUnknownType() {
        new DefaultValueGenerator(null).generate(Test.class);
    }

}
