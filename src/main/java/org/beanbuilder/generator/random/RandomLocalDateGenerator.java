/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package org.beanbuilder.generator.random;

import java.time.LocalDate;

import org.beanbuilder.generator.ValueGenerator;

/**
 * 
 *
 * @author Jeroen van Schagen
 * @since Oct 14, 2014
 */
public class RandomLocalDateGenerator extends RandomSupport implements ValueGenerator {
    
    private final int FIRST_MONTH = 1;
    private final int LAST_MONTH = 12;

    private final int FIRST_DAY = 1;
    
    private final LocalDate min;

    private final LocalDate max;

    public RandomLocalDateGenerator(LocalDate min, LocalDate max) {
        this.min = min;
        this.max = max;
    }
    
    public static RandomLocalDateGenerator forNextYears(int numberOfYears) {
        LocalDate current = LocalDate.now();
        return new RandomLocalDateGenerator(current, current.plusYears(numberOfYears));
    }

    @Override
    public LocalDate generate(Class<?> type) {
        int year = randomInt(min.getYear(), max.getYear());
        int month = randomMonth(year);
        int dayOfMonth = randomDay(year, month);
        return LocalDate.of(year, month, dayOfMonth);
    }

    private int randomMonth(int year) {
        int minMonth = FIRST_MONTH;
        int maxMonth = LAST_MONTH;
        if (year == min.getYear()) {
            minMonth = min.getMonthValue();
        }
        if (year == max.getYear()) {
            maxMonth = max.getMonthValue();
        }
        return randomInt(minMonth, maxMonth);
    }
    
    private int randomDay(int year, int month) {
        int minDay = FIRST_DAY;
        int maxDay = getNumberOfDays(year, month);
        if (year == min.getYear() && month == min.getMonthValue()) {
            minDay = min.getDayOfMonth();
        }
        if (year == max.getYear() && month == max.getMonthValue()) {
            maxDay = max.getDayOfMonth();
        }
        return randomInt(minDay, maxDay);
    }
    
    private int getNumberOfDays(int year, int month) {
        LocalDate date = LocalDate.of(year, month, FIRST_DAY);
        return date.getMonth().length(true);
    }

}
