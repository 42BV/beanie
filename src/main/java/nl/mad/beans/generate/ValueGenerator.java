package nl.mad.beans.generate;

/**
 * Generates a value.
 * @author Jeroen van Schagen
 */
public interface ValueGenerator {

    /**
     * Generate a new value of the specified type.
     * @param valueType the type of value
     * @return the generation value
     */
    Object generate(Class<?> valueType);
	
}
