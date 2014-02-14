package org.beanbuilder.generate;

import org.beanbuilder.support.Classes;
import org.springframework.beans.BeanUtils;

public class NoArgEmptyBeanGenerator implements ValueGenerator {
	
	@Override
	public Object generate(Class<?> valueType) {
		Object object = null;
		if (Classes.hasNullaryConstructor(valueType)) {
			object = BeanUtils.instantiateClass(valueType);
        }
		return object;
	}
	
}