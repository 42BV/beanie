/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package io.dynamo.save;

/**
 * Throws an exception when trying to save beans.
 *
 * @author Jeroen van Schagen
 * @since Feb 14, 2014
 */
public class NoOperationBeanSaver implements BeanSaver {

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T save(T bean) {
        return bean;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(Object bean) {
    }

}
