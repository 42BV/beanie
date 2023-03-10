/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.beanie.generator.random;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 
 *
 * @author jeroen
 * @since Oct 14, 2014
 */
public class RandomLocalDateTimeGeneratorTest {
    
    @Test
    public void testGenerate() {
        LocalDate min = LocalDate.of(2012, 5, 6);
        LocalDate max = LocalDate.of(2014, 2, 19);
        
        RandomLocalDateTimeGenerator generator = new RandomLocalDateTimeGenerator(min, max);
        for (int i = 0; i < 50; i++) {
            LocalDateTime time = generator.generate(null);
            Assertions.assertNotNull(time);
            Assertions.assertFalse(time.isBefore(min.atStartOfDay()));
            Assertions.assertTrue(time.isBefore(max.plusDays(1).atStartOfDay()));
        }
    }

}
