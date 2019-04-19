/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.beanie;

import nl._42.beanie.domain.SimpleBean;
import nl._42.beanie.generator.ValueGenerator;

import java.util.Set;

import static java.lang.String.format;

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
     * Adds a hobby to the hobbies.
     * 
     * @param hobby the hobby
     * @return this instance, for chaining
     */
    SimpleBeanBuildCommand withHobbies(String hobby);
    
    /**
     * Adds a hobby to the hobbies.
     * 
     * @param hobies the hobies
     * @return this instance, for chaining
     */
    SimpleBeanBuildCommand withHobbies(Set<String> hobies);
    
    /**
     * Default implementation, changes the name to a constant value.
     * 
     * @return this instance, for chaining
     */
    default SimpleBeanBuildCommand useDefaultName() {
        return this.withName("Default");
    }

    /**
     * Default implementation, changes the name to a constant value.
     *
     * @param firstName the first name
     * @param lastName the last name
     * @return this instance, for chaining
     */
    default SimpleBeanBuildCommand withName(String firstName, String lastName) {
        return this.withName(
          format("%s %s", firstName, lastName)
        );
    }

}
