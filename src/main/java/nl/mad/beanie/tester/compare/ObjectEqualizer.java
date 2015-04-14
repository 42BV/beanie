/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl.mad.beanie.tester.compare;

/**
 * Compares two objects with each other.
 *
 * @author Jeroen van Schagen
 * @since Mar 10, 2014
 */
public interface ObjectEqualizer {
    
    /**
     * Determine if two objects are sementically equal.
     * 
     * @param left the left object
     * @param right the right object
     * @return {@code true} when equal, else {@code false}
     */
    boolean isEqual(Object left, Object right);

}