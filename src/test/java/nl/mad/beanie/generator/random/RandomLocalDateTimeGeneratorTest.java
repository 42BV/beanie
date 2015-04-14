/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl.mad.beanie.generator.random;

import java.time.LocalDate;
import java.time.LocalDateTime;

import nl.mad.beanie.generator.random.RandomLocalDateTimeGenerator;

import org.junit.Assert;
import org.junit.Test;

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
            Assert.assertNotNull(time);
            Assert.assertFalse(time.isBefore(min.atStartOfDay()));
            Assert.assertTrue(time.isBefore(max.plusDays(1).atStartOfDay()));
        }
    }

}
