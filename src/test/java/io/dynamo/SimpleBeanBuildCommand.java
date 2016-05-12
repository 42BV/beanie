/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.dynamo;

import io.dynamo.domain.SimpleBean;
import io.dynamo.generator.ValueGenerator;

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
    SimpleBeanBuildCommand withName(String name);
    
    /**
     * Changes the name with a generator.
     * 
     * @param generator the generator
     * @return this instance, for chaining
     */
    SimpleBeanBuildCommand withName(ValueGenerator generator);

    /**
     * Changes the name with a registered generator.
     * 
     * @return this instance, for chaining
     */
    SimpleBeanBuildCommand withNestedBean();
    
    /**
     * Default implementation, changes the name to a constant value.
     * 
     * @return this instance, for chaining
     */
    default SimpleBeanBuildCommand withDefaultName() {
        return this.withName("Default");
    }

}
