package io.dynamo;

import io.dynamo.util.PropertyReference;

/**
 * 
 *
 * @author Jeroen van Schagen
 * @since Jun 8, 2016
 */
public class SimplePropertyValueGenerator extends PropertyValueGenerator {
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object generate(PropertyReference reference, Class<?> propertyType) {
        return "another '" + reference.getPropertyName() + "'";
    }
    
}
