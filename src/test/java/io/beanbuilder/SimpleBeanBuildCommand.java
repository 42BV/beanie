/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.beanbuilder;

import io.beanbuilder.domain.SimpleBean;
import io.beanbuilder.generator.ValueGenerator;

/**
 * Build command for simple beans.
 *
 * @author Jeroen van Schagen
 * @since Mar 26, 2015
 */
public interface SimpleBeanBuildCommand extends EditableBeanBuildCommand<SimpleBean> {

    /**
     * Changes the name with a value.
     * 
     * @param name the name
     * @return this instance, for chaining
     */
    SimpleBeanBuildCommand setName(String name);
    
    /**
     * Changes the name with a generator.
     * 
     * @param generator the generator
     * @return this instance, for chaining
     */
    SimpleBeanBuildCommand setName(ValueGenerator generator);

    /**
     * Changes the name with a registered generator.
     * 
     * @return this instance, for chaining
     */
    @SetProperty("nestedBean")
    SimpleBeanBuildCommand withNestedBean();

}
