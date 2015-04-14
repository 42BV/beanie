/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl.mad.beanie.generator.random;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import nl.mad.beanie.generator.ValueGenerator;

/**
 * 
 *
 * @author jeroen
 * @since Oct 14, 2014
 */
public class RandomLocalDateTimeGenerator extends RandomSupport implements ValueGenerator {

    private final RandomLocalTimeGenerator timeGenerator = new RandomLocalTimeGenerator();

    private final LocalDate min;
    
    private final LocalDate max;
    
    public RandomLocalDateTimeGenerator(LocalDate min, LocalDate max) {
        this.min = min;
        this.max = max;
    }
    
    @Override
    public LocalDateTime generate(Class<?> type) {
        LocalDate date = new RandomLocalDateGenerator(min, max).generate(type);
        LocalTime time = timeGenerator.generate(type);
        return LocalDateTime.of(date, time);
    }
    
}
