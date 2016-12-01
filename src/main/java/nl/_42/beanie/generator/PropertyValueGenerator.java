package nl._42.beanie.generator;

import nl._42.beanie.util.PropertyReference;

/**
 * Value generator that is capable of providing the property.
 *
 * @author Jeroen van Schagen
 * @since Jun 8, 2016
 */
public abstract class PropertyValueGenerator implements ValueGenerator {
    
    /**
     * Generate a new value of the specified property.
     * 
     * @param reference the property reference
     * @param propertyType the type of property
     * @return the generation property value
     */
    public abstract Object generate(PropertyReference reference, Class<?> propertyType);
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final Object generate(Class<?> type) {
        throw new UnsupportedOperationException("Should use the property aware generate method.");
    }

}
