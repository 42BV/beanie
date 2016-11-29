/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.beanie.generator.random;

import java.math.BigDecimal;
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
    
    public boolean randomBoolean(double d) {
        return randomDouble() < d;
    }
    
    public int randomInt(int minimum, int maximum) {
        return minimum + randomInt(maximum - minimum);
    }
    
    public long randomLong(int maximum) {
        return randomLong(1, maximum);
    }
    
    public long randomLong(int minimum, int maximum) {
        return minimum + randomInt(maximum - minimum);
    }
    
    public double randomDouble() {
        return RANDOM.nextDouble();
    }
    
    public double randomDouble(double maximum) {
        return maximum * randomDouble();
    }

    public BigDecimal randomBigDecimal() {
        return randomBigDecimal(1);
    }
    
    public BigDecimal randomBigDecimal(double maximum) {
        return BigDecimal.valueOf(randomDouble(maximum));
    }

}
