package nl._42.beanie.generator.supported;

import java.lang.reflect.AccessibleObject;

/**
 * Determines if something is supported for this accessible object.
 *
 * @author Jeroen van Schagen
 * @since Jun 8, 2016
 */
public interface Supportable {
    
    /**
     * Determine if this accessible object is supported.
     * 
     * @param object the object
     * @return whether it is supported
     */
    boolean supports(AccessibleObject object);

}
