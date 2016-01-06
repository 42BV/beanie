package io.dynamo.domain;

import java.util.Date;

public class FullBean extends ParentBean {

    private Integer integerValue;

    private Object[] arrayValue;

    private Date dateValue;

    private SomeEnum enumValue;

    private SomeInterface interfaceValue;
    
    private EmptyBean noArgBean;
    
    private NestedBeanWithConstructor complexBean;
    
	private String unsupportedValue;
	
	private String differentValue;
	
	private int primitiveValue;
	
	private EmptyBean differentTypeValue;

    public Integer getIntegerValue() {
        return integerValue;
    }

    public void setIntegerValue(Integer integerValue) {
        this.integerValue = integerValue;
    }

    public Object[] getArrayValue() {
        return arrayValue != null ? arrayValue.clone() : null;
    }

    public void setArrayValue(Object[] arrayValue) {
        this.arrayValue = arrayValue != null ? arrayValue.clone() : null;
    }

    public Date getDateValue() {
        return dateValue != null ? (Date) dateValue.clone() : null;
    }

    public void setDateValue(Date dateValue) {
        this.dateValue = dateValue != null ? (Date) dateValue.clone() : null;
    }

    public SomeEnum getEnumValue() {
        return enumValue;
    }

    public void setEnumValue(SomeEnum enumValue) {
        this.enumValue = enumValue;
    }

    public SomeInterface getInterfaceValue() {
        return interfaceValue;
    }

    public void setInterfaceValue(SomeInterface interfaceValue) {
        this.interfaceValue = interfaceValue;
    }
    
    public EmptyBean getNoArgBean() {
		return noArgBean;
	}
    
    public void setNoArgBean(EmptyBean noArgBean) {
		this.noArgBean = noArgBean;
	}
    
    public NestedBeanWithConstructor getComplexBean() {
		return complexBean;
	}
    
    public void setComplexBean(NestedBeanWithConstructor complexBean) {
		this.complexBean = complexBean;
	}
    
	public String getUnsupportedValue() {
		return unsupportedValue;
	}
	
	public void setUnsupportedValue(String unsupportedValue) {
		throw new UnsupportedOperationException();
	}
	
	public String getDifferentValue() {
		return differentValue;
	}
	
	public void setDifferentValue(String differentValue) {
		this.differentValue = "changed " + differentValue;
	}
	
	public int getPrimitiveValue() {
		return primitiveValue;
	}
	
	public void setPrimitiveValue(int primitiveValue) {
		this.primitiveValue = primitiveValue;
	}
	
	public EmptyBean getDifferentTypeValue() {
		return differentTypeValue;
	}
	
	public void setDifferentTypeValue(EmptyBean differentTypeValue) {
		this.differentTypeValue = new EmptyChildBean();
	}

}