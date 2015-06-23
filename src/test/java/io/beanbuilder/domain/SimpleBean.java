package io.beanbuilder.domain;

public class SimpleBean {
    
    private Long id;

    private String name;
    
    private String shortName;

	private NestedBean nestedBean;
	
	private NestedBeanWithConstructor nestedBeanWithConstructor;
    
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getShortName() {
        return shortName;
    }
    
    public void setShortName(String shortName) {
        this.shortName = shortName;
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
