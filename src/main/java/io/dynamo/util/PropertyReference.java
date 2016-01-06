package io.dynamo.util;

import java.beans.PropertyDescriptor;

public class PropertyReference {

	private final Class<?> declaringClass;
	
	private final String propertyName;
    
    public PropertyReference(PropertyDescriptor description) {
        if (description.getWriteMethod() != null) {
            this.declaringClass = description.getWriteMethod().getDeclaringClass();
        } else {
            this.declaringClass = description.getReadMethod().getDeclaringClass();
        }
        this.propertyName = description.getName();
    }

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
        return declaringClass.hashCode() * propertyName.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
        if (obj instanceof PropertyReference) {
            PropertyReference other = (PropertyReference) obj;
            return Objects.equals(declaringClass, other.declaringClass) && Objects.equals(propertyName, other.propertyName);
        } else {
            return false;
        }
	}
	
	@Override
	public String toString() {
		return declaringClass.getName() + "." + propertyName;
	}
	
}
