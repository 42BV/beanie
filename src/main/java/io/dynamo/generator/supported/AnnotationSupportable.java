package io.dynamo.generator.supported;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;

/**
 * Java8 predicate based supportable implementation.
 *
 * @author Jeroen van Schagen
 * @since Jun 8, 2016
 */
public class AnnotationSupportable implements Supportable {
    
    private final Class<? extends Annotation> annotationType;
    
    public AnnotationSupportable(Class<? extends Annotation> annotationType) {
        this.annotationType = annotationType;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(AccessibleObject object) {
        return object.getAnnotationsByType(annotationType).length != 0;
    }

}
