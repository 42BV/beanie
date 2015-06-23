/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.beanbuilder.generator.random;

import io.beanbuilder.generator.ValueGenerator;

import java.time.LocalTime;

/**
 * 
 *
 * @author jeroen
 * @since Oct 14, 2014
 */
public class RandomLocalTimeGenerator extends RandomSupport implements ValueGenerator {
    
    private final int FIRST_HOUR = 1;
    private final int LAST_HOUR = 24;
    
    private final int FIRST_MINUTE = 1;
    private final int LAST_MINUTE = 60;

    private final int FIRST_SECOND = 1;
    private final int LAST_SECOND = 60;
    
    @Override
    public LocalTime generate(Class<?> type) {
        int hour = randomInt(FIRST_HOUR, LAST_HOUR);
        int minute = randomInt(FIRST_MINUTE, LAST_MINUTE);
        int second = randomInt(FIRST_SECOND, LAST_SECOND);
        return LocalTime.of(hour, minute, second);
    }
    
}
