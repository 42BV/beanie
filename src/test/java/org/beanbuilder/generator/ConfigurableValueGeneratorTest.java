/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package org.beanbuilder.generator;

import org.beanbuilder.generate.ConfigurableValueGenerator;
import org.junit.Test;

public class ConfigurableValueGeneratorTest {
    
    @Test(expected = IllegalArgumentException.class)
    public void testUnknownType() {
        new ConfigurableValueGenerator(null).generate(Test.class);
    }

}
