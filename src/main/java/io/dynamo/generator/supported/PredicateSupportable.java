package io.dynamo.generator.supported;

import java.lang.reflect.AccessibleObject;
import java.util.function.Predicate;

/**
 * Java8 predicate based supportable implementation.
 *
 * @author Jeroen van Schagen
 * @since Jun 8, 2016
 */
public class PredicateSupportable implements Supportable {
    
    private final Predicate<AccessibleObject> predicate;
    
    public PredicateSupportable(Predicate<AccessibleObject> predicate) {
        this.predicate = predicate;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(AccessibleObject object) {
        return predicate.test(object);
    }

}
