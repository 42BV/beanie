package org.beanbuilder.generator;

import java.lang.reflect.Array;

public class EmptyArrayValueGenerator implements ValueGenerator {

	@Override
	public Object generate(Class<?> valueType) {
        Class<?> componentType = valueType.getComponentType();
        return Array.newInstance(componentType, 0);
	}

}