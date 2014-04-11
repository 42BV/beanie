/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package org.beanbuilder.generator;

import org.beanbuilder.generator.DefaultConfigurableValueGenerator;
import org.junit.Test;

public class DefaultConfigurableValueGeneratorTest {
    
    @Test(expected = IllegalArgumentException.class)
    public void testUnknownType() {
        new DefaultConfigurableValueGenerator(null).generate(Test.class);
    }

}
