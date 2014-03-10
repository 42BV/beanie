/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package org.beanbuilder.tester;

import java.math.BigDecimal;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ObjectUtils;

/**
 * Compares two objects with each other.
 *
 * @author Jeroen van Schagen
 * @since Mar 10, 2014
 */
public class ObjectEqualizer {
    
    /**
     * Determine if two objects are sementically equal.
     * 
     * @param left the left object
     * @param right the right object
     * @return {@code true} when equal, else {@code false}
     */
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
