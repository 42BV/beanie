package nl._42.beanie.domain;

import nl._42.beanie.SimpleAnnotation;

import java.util.HashSet;
import java.util.Set;

public class SimpleBean {
    
    private Long id;

    private String name;
    
    private String shortName;

	private NestedBean nestedBean;
	
	private NestedBeanWithConstructor nestedBeanWithConstructor;
    
    private Set<String> hobbies = new HashSet<>();
    
    @SimpleAnnotation
    private String annotated;

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
    
    public Set<String> getHobbies() {
        return hobbies;
    }
    
    public String getAnnotated() {
        return annotated;
    }
    
    public void setAnnotated(String annotated) {
        this.annotated = annotated;
    }
	
}
