package org.beanbuilder.support;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class PropertyReference {

	private final Class<?> declaringClass;
	
	private final String propertyName;

	public PropertyReference(Class<?> declaringClass, String propertyName) {
		this.declaringClass = declaringClass;
		this.propertyName = propertyName;
	}

	public Class<?> getDeclaringClass() {
		return declaringClass;
	}
	
	public String getPropertyName() {
		return propertyName;
	}
	
	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}
	
	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}
	
	@Override
	public String toString() {
		return declaringClass.getName() + "." + propertyName;
	}
	
}
