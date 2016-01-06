/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.dynamo.generator;

/**
 * Value generator that always throws an {@link UnsupportedOperationException}.
 *
 * @author Jeroen van Schagen
 * @since May 13, 2015
 */
public class UnsupportedValueGenerator implements ValueGenerator {
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object generate(Class<?> type) {
        throw new UnsupportedOperationException("Could not instantiate value");
    }
    
}
