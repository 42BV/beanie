/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package org.beanbuilder.generator;

import org.beanbuilder.generate.TypeValueGenerator;
import org.junit.Test;

public class TypeValueGeneratorTest {
    
    @Test(expected = IllegalArgumentException.class)
    public void testUnknownType() {
        new TypeValueGenerator(null).generate(Test.class);
    }

}
