/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package nl.mad.beanie.save;

/**
 * Throws an exception when trying to save beans.
 *
 * @author Jeroen van Schagen
 * @since Feb 14, 2014
 */
public class UnsupportedBeanSaver implements BeanSaver {

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T save(T bean) {
        throw new UnsupportedOperationException("Saving bean is not supported.");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(Object bean) {
        throw new UnsupportedOperationException("Deleting bean is not supported.");
    }

}
