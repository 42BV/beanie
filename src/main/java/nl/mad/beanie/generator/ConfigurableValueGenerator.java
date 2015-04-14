package nl.mad.beanie.generator;

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

    private final ValueGenerator fallback;

    public ConfigurableValueGenerator(ValueGenerator fallback) {
    	generators = new LinkedHashMap<Class<?>, ValueGenerator>();
        this.fallback = fallback;
    }

	@Override
    public Object generate(Class<?> type) {
		ValueGenerator generator = getSupportedGenerator(type);
        if (generator == null) {
            if (fallback == null) {
                throw new IllegalArgumentException("Could not generate value for '" + type.getName() + "'.");
            }
            generator = fallback;
        }
        return generator.generate(type);
    }

    private ValueGenerator getSupportedGenerator(Class<?> type) {
    	ValueGenerator generator = generators.get(type);
    	if (generator == null) {
        	generator = findFirstAssignableGenerator(type);
    	}
        return generator;
    }

	private ValueGenerator findFirstAssignableGenerator(Class<?> type) {
        ValueGenerator generator = null;
		for (Entry<Class<?>, ValueGenerator> entry : generators.entrySet()) {
            if (entry.getKey().isAssignableFrom(type)) {
            	generator = entry.getValue();
            	break;
            }
        }
		return generator;
	}

    /**
     * Register a value generation strategy for a specific type.
     * 
     * @param type the type of value
     * @param generator the generation strategy
     * @return this instance
     */
    public ConfigurableValueGenerator register(Class<?> type, ValueGenerator generator) {
        generators.put(type, generator);
        return this;
    }

    /**
     * Register a constant value for a specific type.
     * 
     * @param type the type of value
     * @param value the value to return
     * @return this instance
     */
    public ConfigurableValueGenerator registerValue(Class<?> type, Object value) {
        return register(type, new ConstantValueGenerator(value));
    }
    
    /**
     * Determine if the value is known in our mapping.
     * 
     * @param type the type of value
     * @return if it exists
     */
    public boolean contains(Class<?> type) {
        return getSupportedGenerator(type) != null;
    }

}
