/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.beanbuilder;

import io.beanbuilder.domain.SimpleBean;

/**
 * Build command for simple beans.
 *
 * @author Jeroen van Schagen
 * @since Mar 26, 2015
 */
@BeanBuildConfig(preffix = "with")
public interface WithSimpleBeanBuildCommand extends EditableBeanBuildCommand<SimpleBean> {

    /**
     * Changes the name with a value.
     * 
     * @param name the name
     * @return this instance, for chaining
     */
    WithSimpleBeanBuildCommand withName(String name);

}
