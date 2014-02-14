/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package org.beanbuilder.save;

/**
 * Throws an exception when trying to save beans.
 *
 * @author jeroen
 * @since Feb 14, 2014
 */
public class UnsupportedBeanSaver implements BeanSaver {

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T save(T bean) {
        throw new UnsupportedOperationException("Could not save bean.");
    }
    
}
