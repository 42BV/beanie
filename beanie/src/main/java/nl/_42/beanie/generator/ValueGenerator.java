package nl._42.beanie.generator;

/**
 * Generates a value.
 * 
 * @author Jeroen van Schagen
 */
@FunctionalInterface
public interface ValueGenerator {

    /**
     * Generate a new value of the specified type.
     * 
     * @param type the type of value
     * @return the generation value
     */
    Object generate(Class<?> type);
	
}
