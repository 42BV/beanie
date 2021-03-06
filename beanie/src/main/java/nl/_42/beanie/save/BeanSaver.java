/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.beanie.save;

/**
 * Saves the generated beans.
 *
 * @author Jeroen van Schagen
 * @since Feb 14, 2014
 */
public interface BeanSaver {
    
    /**
     * Saves a bean.
     *
     * @param <T> the bean type
     * @param bean the bean to save
     * @return the saved bean
     */
    <T> T save(T bean);

    /**
     * Deletes a bean.
     * 
     * @param bean the bean to delete
     */
    void delete(Object bean);

}
