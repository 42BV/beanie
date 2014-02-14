/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package org.beanbuilder.generate.construction;

import java.lang.reflect.Constructor;

/**
 * Selects the constructor that should be used for bean construction. 
 *
 * @author jeroen
 * @since Feb 14, 2014
 */
public interface ConstructorStrategy {
    
    Constructor<?> getConstructor(Class<?> beanClass);

}
