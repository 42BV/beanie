/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package org.beanbuilder.generate.constructor;

import java.lang.reflect.Constructor;

/**
 * Selects the shortest constructor.
 *
 * @author Jeroen van Schagen
 * @since Feb 14, 2014
 */
public class ShortestConstructorStrategy implements ConstructorStrategy {
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Constructor<?> getConstructor(Class<?> beanClass) {
        Constructor<?> shortest = null;
        for (Constructor<?> constructor : beanClass.getDeclaredConstructors()) {
            if (shortest == null || shortest.getParameterTypes().length > constructor.getParameterTypes().length) {
                shortest = constructor;
            }
        }
        return shortest;
    }

}
