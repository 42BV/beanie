/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package io.dynamo.generator;

import io.dynamo.generator.DefaultValueGenerator;

import org.junit.Test;

public class DefaultConfigurableValueGeneratorTest {
    
    @Test(expected = IllegalArgumentException.class)
    public void testUnknownType() {
        new DefaultValueGenerator(null).generate(Test.class);
    }

}
