package org.beanbuilder.generate;

import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;

import org.beanbuilder.support.PropertyReference;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

/**
 * Generates a new bean, filled with values, using the
 * behavior registered to that property or value type.
 * @author Jeroen van Schagen
 */
public final class BeanGenerator implements ValueGenerator {

	private final Map<PropertyReference, ValueGenerator> propertyGenerators;
	
	private final ConfigurableValueGenerator typeGenerator;
    
    private final ValueGenerator beanConstructor;
	
	public BeanGenerator() {
		propertyGenerators = new HashMap<PropertyReference, ValueGenerator>();
		typeGenerator = new ConfigurableValueGenerator(this);
        beanConstructor = new ShortestConstructorBeanGenerator(typeGenerator);
	}
	
	@Override
	public Object generate(Class<?> beanClass) {
		if (typeGenerator.contains(beanClass)) {
            return typeGenerator.generate(beanClass);
		} else {
            return generateBean(beanClass);
		}
	}

	private Object generateBean(Class<?> beanClass) {
        Object bean = beanConstructor.generate(beanClass);
        setProperties(bean);
		return bean;
	}

    private void setProperties(Object bean) {
        BeanWrapper beanWrapper = new BeanWrapperImpl(bean);
        for (PropertyDescriptor propertyDescriptor : beanWrapper.getPropertyDescriptors()) {
        	if (isGeneratableProperty(propertyDescriptor)) {
                Object value = generatePropertyValue(bean.getClass(), propertyDescriptor);
                beanWrapper.setPropertyValue(propertyDescriptor.getName(), value);
        	}
        }
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
			return typeGenerator;
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
    	typeGenerator.register(valueType, generator);
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
