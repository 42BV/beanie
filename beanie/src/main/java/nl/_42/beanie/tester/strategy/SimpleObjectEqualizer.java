/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.beanie.tester.strategy;

import nl._42.beanie.util.Objects;

import java.math.BigDecimal;

/**
 * Simple implementation of {@link ObjectEqualizer}.
 *
 * @author Jeroen van Schagen
 * @since Mar 10, 2014
 */
public class SimpleObjectEqualizer implements ObjectEqualizer {
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("deprecation")
    public boolean isEqual(Object left, Object right) {
        boolean equals = false;
        if (left == right) {
            equals = true;
        } else if (left != null && right != null && left.getClass().equals(right.getClass())) {
            if (left.getClass().isArray()) {
                equals = org.apache.commons.lang3.ArrayUtils.isEquals(left, right);
            } else if (left instanceof BigDecimal) {
                equals = ((BigDecimal) left).compareTo((BigDecimal) right) == 0;
            } else {
                equals = Objects.equals(left, right);
            }
        }
        return equals;
    }

}
