package org.beanbuilder.sample;

public class SimpleBean {

	private String value;
	
	private NestedBean nestedBean;
	
	private NestedBeanWithConstructor nestedBeanWithConstructor;
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public NestedBean getNestedBean() {
		return nestedBean;
	}
	
	public void setNestedBean(NestedBean nestedBean) {
		this.nestedBean = nestedBean;
	}
	
	public NestedBeanWithConstructor getNestedBeanWithConstructor() {
		return nestedBeanWithConstructor;
	}
	
	public void setNestedBeanWithConstructor(NestedBeanWithConstructor nestedBeanWithConstructor) {
		this.nestedBeanWithConstructor = nestedBeanWithConstructor;
	}
	
}
