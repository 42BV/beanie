/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package org.beanbuilder.generator.random;

import java.util.Random;

/**
 * Support class for generating random values. 
 *
 * @author Jeroen van Schagen
 * @since Feb 14, 2014
 */
abstract class RandomSupport {
    
    private static final Random RANDOM = new Random();
    
    public int randomInt(int n) {
        return RANDOM.nextInt(n);
    }

}
