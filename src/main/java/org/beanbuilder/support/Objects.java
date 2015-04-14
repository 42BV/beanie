/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package org.beanbuilder.support;

/**
 * Temporary class until everybody moves to Java8.
 *
 * @author Jeroen van Schagen
 * @since Apr 14, 2015
 */
public class Objects {
    
    /**
     * Compares two objects null-safe.
     * 
     * @param a the left object
     * @param b the right object
     * @return {@code true} when equal, else {@code false}
     */
    public static boolean equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }

}
