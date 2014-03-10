/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package org.beanbuilder.save;

/**
 * Saves the generated beans.
 *
 * @author jeroen
 * @since Feb 14, 2014
 */
public interface ValueSaver {
    
    <T> T save(T value);

}
