package org.beanbuilder.generator;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Generates values of any type, using the behavior registered to that value type.
 * 
 * @author Jeroen van Schagen
 */
public class ConfigurableValueGenerator implements ValueGenerator {

    private final Map<Class<?>, ValueGenerator> generators;

    private final ValueGenerator fallbackGenerator;

    public ConfigurableValueGenerator(ValueGenerator fallbackGenerator) {
    	generators = new LinkedHashMap<Class<?>, ValueGenerator>();
        this.fallbackGenerator = fallbackGenerator;
    }

	@Override
    public Object generate(Class<?> valueType) {
		ValueGenerator generator = getSupportedGenerator(valueType);
        if (generator == null) {
            if (fallbackGenerator == null) {
                throw new IllegalArgumentException("Could not generate value for '" + valueType.getName() + "'.");
            }
            generator = fallbackGenerator;
        }
        return generator.generate(valueType);
    }

    private ValueGenerator getSupportedGenerator(Class<?> valueType) {
    	ValueGenerator generator = generators.get(valueType);
    	if (generator == null) {
        	generator = findFirstAssignableGenerator(valueType);
    	}
        return generator;
    }

	private ValueGenerator findFirstAssignableGenerator(Class<?> valueType) {
        ValueGenerator generator = null;
		for (Entry<Class<?>, ValueGenerator> entry : generators.entrySet()) {
            if (entry.getKey().isAssignableFrom(valueType)) {
            	generator = entry.getValue();
            	break;
            }
        }
		return generator;
	}

    /**
     * Register a value generation strategy for a specific type.
     * 
     * @param valueType the type of value
     * @param generator the generation strategy
     * @return this instance
     */
    public ConfigurableValueGenerator register(Class<?> valueType, ValueGenerator generator) {
        generators.put(valueType, generator);
        return this;
    }

    /**
     * Register a constant value for a specific type.
     * 
     * @param valueType the type of value
     * @param value the value to return
     * @return this instance
     */
    public ConfigurableValueGenerator registerValue(Class<?> valueType, Object value) {
        return register(valueType, new ConstantValueGenerator(value));
    }
    
    /**
     * Determine if the value is known in our mapping.
     * 
     * @param valueType the type of value
     * @return if it exists
     */
    public boolean contains(Class<?> valueType) {
        return getSupportedGenerator(valueType) != null;
    }

}
