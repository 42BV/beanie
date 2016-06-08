package io.dynamo.generator;

import io.dynamo.BeanBuilder;
import io.dynamo.util.PropertyReference;

import java.lang.reflect.Field;

import org.springframework.data.domain.Persistable;
import org.springframework.util.ReflectionUtils;

/**
 * Dynamically generates a value of the referenced type.
 *
 * @author Jeroen van Schagen
 * @since Jun 8, 2016
 */
public class ReferencedTypeValueGenerator extends PropertyValueGenerator {
    
    private final BeanBuilder generator;
    
    public ReferencedTypeValueGenerator(BeanBuilder generator) {
        this.generator = generator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("rawtypes")
    public Object generate(PropertyReference reference, Class<?> propertyType) {
        Field field = ReflectionUtils.findField(reference.getDeclaringClass(), reference.getPropertyName());
        Class<?> entityType = field.getAnnotation(ReferencedType.class).value();
        Object entity = generator.start(entityType).fill().save();
        return ((Persistable) entity).getId();
    }
    
}
