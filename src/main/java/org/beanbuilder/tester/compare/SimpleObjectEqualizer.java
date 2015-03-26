/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package org.beanbuilder.tester.compare;

import java.math.BigDecimal;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ObjectUtils;

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
    public boolean isEqual(Object left, Object right) {
        boolean equals = false;
        if (left == right) {
            equals = true;
        } else if (left != null && right != null && left.getClass().equals(right.getClass())) {
            if (left.getClass().isArray()) {
                equals = ArrayUtils.isEquals(left, right);
            } else if (left instanceof BigDecimal) {
                equals = ((BigDecimal) left).compareTo((BigDecimal) right) == 0;
            } else {
                equals = ObjectUtils.equals(left, right);
            }
        }
        return equals;
    }

}
