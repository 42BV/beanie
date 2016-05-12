/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package io.dynamo.generator.random;

import io.dynamo.generator.ValueGenerator;

/**
 * Generates a random string within the specified length range
 * and using only the specified letters.
 *
 * @author Sander Benschop
 * @since Feb 14, 2014
 */
public class RandomStringGenerator extends RandomSupport implements ValueGenerator {
    
    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private final RandomIntegerGenerator lengthGenerator;
    
    private final String letters;
    
    public RandomStringGenerator(int maximum) {
        this(Math.min(maximum, 1), maximum);
    }

    public RandomStringGenerator(int minimum, int maximum) {
        this(minimum, maximum, LETTERS);
    }

    public RandomStringGenerator(int minimum, int maximum, String letters) {
        this.lengthGenerator = new RandomIntegerGenerator(minimum, maximum);
        this.letters = letters;
    }
    
    @Override
    public Object generate(Class<?> type) {
        int length = lengthGenerator.generate(null);
        return randomString(length);
    }
    
    public String randomString(int length) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            result.append(randomCharOf(letters));
        }
        return result.toString();
    }
    
    private char randomCharOf(String values) {
        return values.charAt(randomInt(values.length()));
    }

}
