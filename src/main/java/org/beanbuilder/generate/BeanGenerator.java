package org.beanbuilder.generate;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.beanbuilder.PropertyReference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

/**
 * Generates a new bean, filled with values, using the
 * behavior registered to that property or value type.
 * @author Jeroen van Schagen
 */
public final class BeanGenerator implements ValueGenerator {

	private final Map<PropertyReference, ValueGenerator> propertyGenerators;
	
	private final TypeValueGenerator typeValueGenerator;
	
	public BeanGenerator() {
		propertyGenerators = new HashMap<PropertyReference, ValueGenerator>();
		typeValueGenerator = new TypeValueGenerator(this);
	}
	
	@Override
	public Object generate(Class<?> beanClass) {
		Object bean;
		if (typeValueGenerator.contains(beanClass)) {
			// Check if a specific generator is registered for this type
			bean = typeValueGenerator.generate(beanClass);
		} else {
			bean = generateBean(beanClass);
		}
		return bean;
	}

	private Object generateBean(Class<?> beanClass) {
		Object bean = instantiateBean(beanClass);
        BeanWrapper beanWrapper = new BeanWrapperImpl(bean);
        for (PropertyDescriptor propertyDescriptor : beanWrapper.getPropertyDescriptors()) {
        	if (isGeneratableProperty(propertyDescriptor)) {
        		Object value = generatePropertyValue(beanClass, propertyDescriptor);
                beanWrapper.setPropertyValue(propertyDescriptor.getName(), value);
        	}
        }
		return bean;
	}

	private Object instantiateBean(Class<?> beanClass) {
		Constructor<?> constructor = getShortestConstructor(beanClass);
		if (constructor != null) {
			Class<?>[] parameterTypes = constructor.getParameterTypes();
			Object[] arguments = new Object[parameterTypes.length];
			for (int index = 0; index < parameterTypes.length; index++) {
				arguments[index] = typeValueGenerator.generate(parameterTypes[index]);
			}
			return BeanUtils.instantiateClass(constructor, arguments);
		} else {
			return BeanUtils.instantiateClass(beanClass);
		}
	}
	
	private Constructor<?> getShortestConstructor(Class<?> beanClass) {
		Constructor<?> shortest = null;
		for (Constructor<?> constructor : beanClass.getDeclaredConstructors()) {
			if (shortest == null || shortest.getParameterTypes().length > constructor.getParameterTypes().length) {
				shortest = constructor;
			}
		}
		return shortest;
	}

	private boolean isGeneratableProperty(PropertyDescriptor propertyDescriptor) {
		return propertyDescriptor.getWriteMethod() != null;
	}

	private Object generatePropertyValue(Class<?> beanClass, PropertyDescriptor propertyDescriptor) {
		ValueGenerator propertyGenerator = getSupportedGenerator(beanClass, propertyDescriptor.getName());
		Class<?> propertyType = propertyDescriptor.getPropertyType();
		return propertyGenerator.generate(propertyType);
	}
	
	private ValueGenerator getSupportedGenerator(Class<?> beanClass, String propertyName) {
		PropertyReference propertyReference = new PropertyReference(beanClass, propertyName);
		if (propertyGenerators.containsKey(propertyReference)) {
			return propertyGenerators.get(propertyReference);
		} else {
			return typeValueGenerator;
		}
	}
	
	/**
     * Register a value generation strategy for a specific property.
	 * @param declaringClass the bean class that declares our property
	 * @param propertyName the name of the property
	 * @param generator the generation strategy
	 * @return this instance
	 */
	public BeanGenerator register(Class<?> declaringClass, String propertyName, ValueGenerator generator) {
		propertyGenerators.put(new PropertyReference(declaringClass, propertyName), generator);
		return this;
	}
	
	/**
     * Register a constant value for a specific property.
	 * @param declaringClass the bean class that declares our property
	 * @param propertyName the name of the property
     * @param value the value to return
	 * @return this instance
	 */
	public BeanGenerator registerValue(Class<?> declaringClass, String propertyName, Object value) {
		return register(declaringClass, propertyName, new ConstantValueGenerator(value));
	}
	
    /**
     * Register a value generation strategy for a specific type.
     * @param valueType the type of value
     * @param generator the generation strategy
     * @return this instance
     */
    public BeanGenerator register(Class<?> valueType, ValueGenerator generator) {
    	typeValueGenerator.register(valueType, generator);
        return this;
    }

    /**
     * Register a constant value for a specific type.
     * @param valueType the type of value
     * @param value the value to return
     * @return this instance
     */
    public BeanGenerator registerValue(Class<?> valueType, Object value) {
        return register(valueType, new ConstantValueGenerator(value));
    }
	
}
