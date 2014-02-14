package nl.mad.beans.generate;

import java.lang.reflect.Array;

public class ArrayValueGenerator implements ValueGenerator {

	@Override
	public Object generate(Class<?> valueType) {
        Class<?> componentType = valueType.getComponentType();
        return Array.newInstance(componentType, 0);
	}

}