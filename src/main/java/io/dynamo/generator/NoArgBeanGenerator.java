package io.dynamo.generator;

import io.dynamo.util.Classes;

import org.springframework.beans.BeanUtils;

/**
 * Generates a bean, when a constructor without arguments is defined.
 *
 * @author Jeroen van Schagen
 * @since Mar 26, 2015
 */
public class NoArgBeanGenerator implements ValueGenerator {
	
    /**
     * {@inheritDoc}
     */
	@Override
	public Object generate(Class<?> valueType) {
		Object object = null;
		if (Classes.hasNullaryConstructor(valueType)) {
			object = BeanUtils.instantiateClass(valueType);
        }
		return object;
	}
	
}