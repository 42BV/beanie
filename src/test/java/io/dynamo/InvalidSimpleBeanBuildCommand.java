/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.dynamo;

import io.dynamo.domain.SimpleBean;

/**
 * Build command for simple beans, should crash on startup.
 *
 * @author Jeroen van Schagen
 * @since Mar 26, 2015
 */
public interface InvalidSimpleBeanBuildCommand extends EditableBeanBuildCommand<SimpleBean> {

    /**
     * Changes the name with a value.
     * 
     * @param name the name
     * @return this instance, for chaining
     */
    InvalidSimpleBeanBuildCommand useName(String name);

}
