package org.beanbuilder.tester;

import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;
import org.beanbuilder.generate.TypeValueGenerator;
import org.beanbuilder.generate.ValueGenerator;
import org.beanbuilder.support.Classes;
import org.beanbuilder.support.PropertyReference;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.TypeFilter;

/**
 * Verifies the getter and setter methods of beans. The retrieved
 * value should be semantically equal to the initially set value.
 * 
 * @author Jeroen van Schagen
 */
public class BeanTester {

    private static final Logger LOGGER = Logger.getLogger(BeanTester.class);

    private final Set<PropertyReference> excludedProperties = new HashSet<PropertyReference>();
    
    private final ClassPathScanningCandidateComponentProvider beanProvider;

    private final ValueGenerator valueGenerator;
    
    private boolean inherit = true;

    public BeanTester() {
        this(new TypeValueGenerator());
    }

    public BeanTester(ValueGenerator valueGenerator) {
        this.beanProvider = new ClassPathScanningCandidateComponentProvider(false);
        this.valueGenerator = valueGenerator;
        excludeProperty(Throwable.class, "stackTrace");
    }

    /**
     * Verify the getter and setters of each bean classes, declared in
     * the same package, or child packages, as the specified class.
     * 
     * @param basePackageClass the base package class
     * @return the number of verified beans
     */
    public int verifyBeans(Class<?> basePackageClass) {
        return verifyBeans(basePackageClass.getPackage().getName());
    }

    /**
     * Verify the getter and setters of each bean classes, declared in
     * the specified package, or child packages.
     * 
     * @param basePackage the base package to search for beans
     * @return the number of verified beans
     */
    public int verifyBeans(String basePackage) {
        Set<BeanDefinition> beanDefinitions = beanProvider.findCandidateComponents(basePackage);
        for (BeanDefinition beanDefinition : beanDefinitions) {
            verifyBean(Classes.forName(beanDefinition.getBeanClassName()));
        }
        return beanDefinitions.size();
    }

    /**
     * Verify the getter and setters of the specified bean.
     * 
     * @param beanClass the bean class
     * @throws InconsistentGetterAndSetterException whenever an inconsistency was found
     */
    public void verifyBean(Class<?> beanClass) {
        LOGGER.debug("Verifying bean: " + beanClass.getName());
        final BeanWrapper beanWrapper = newBeanWrapper(beanClass);

        for (PropertyDescriptor propertyDescriptor : beanWrapper.getPropertyDescriptors()) {
            if (isPropertyToVerify(beanClass, propertyDescriptor)) {
                verifyProperty(beanWrapper, propertyDescriptor);
            }
        }
    }

    private BeanWrapper newBeanWrapper(Class<?> beanClass) {
        Object bean = valueGenerator.generate(beanClass);
        return new BeanWrapperImpl(bean);
    }

    private boolean isPropertyToVerify(Class<?> beanClass, PropertyDescriptor propertyDescriptor) {
        boolean verify = false;
        if (propertyDescriptor.getReadMethod() != null && propertyDescriptor.getWriteMethod() != null) {
        	Class<?> declaringClass = propertyDescriptor.getWriteMethod().getDeclaringClass();
            verify = isDeclaredInBean(beanClass, declaringClass) && isNotExcluded(declaringClass, propertyDescriptor.getName());
        }
        return verify;
    }

	private boolean isDeclaredInBean(Class<?> beanClass, Class<?> declaringClass) {
		return inherit || declaringClass.equals(beanClass);
	}

    private boolean isNotExcluded(Class<?> declaringClass, String propertyName) {
        PropertyReference propertyReference = new PropertyReference(declaringClass, propertyName);
		return ! excludedProperties.contains(propertyReference);
    }

    /**
     * Verify the getter and setter of a property.
     * 
     * @param beanClass the bean class
     * @param propertyName the property name
     */
    public void verifyProperty(Class<?> beanClass, String propertyName) {
        BeanWrapper beanWrapper = newBeanWrapper(beanClass);
        verifyProperty(beanWrapper, beanWrapper.getPropertyDescriptor(propertyName));
    }

    private void verifyProperty(BeanWrapper beanWrapper, PropertyDescriptor propertyDescriptor) {
        final String propertyName = propertyDescriptor.getName();
        final Class<?> propertyType = propertyDescriptor.getPropertyType();
        
        LOGGER.debug("Verifying property '" + propertyName + "' of bean: " + beanWrapper.getWrappedClass().getName());

        // Check with null value
        if (!propertyType.isPrimitive()) {
            verifyPropertyWithValue(beanWrapper, propertyName, null);
        }
        
        // Check with not-null value
        Object generatedValue = valueGenerator.generate(propertyType);
        verifyPropertyWithValue(beanWrapper, propertyName, generatedValue);
    }
    
    private void verifyPropertyWithValue(BeanWrapper beanWrapper, String propertyName, Object value) {
        Object result;

        try {
            beanWrapper.setPropertyValue(propertyName, value);
            result = beanWrapper.getPropertyValue(propertyName);
        } catch (RuntimeException rte) {
            String message = String.format(
                    "Property '%s' of '%s' has an unusable getter and/or setter.",
                    propertyName, beanWrapper.getWrappedClass().getName());
            throw new IllegalStateException(message, rte);
        }

        if (! isEqual(value, result)) {
            String message = String.format(
                    "Property '%s' of '%s' returned a different value than initially set (original: %s, actual: %s).",
                    propertyName, beanWrapper.getWrappedClass().getName(), value, result);
            throw new InconsistentGetterAndSetterException(message);
        }
    }

    private boolean isEqual(Object expected, Object actual) {
        boolean equals = false;
        if (expected == actual) {
            equals = true;
        } else if (expected != null && actual != null && expected.getClass().equals(actual.getClass())) {
            if (expected.getClass().isArray()) {
                equals = ArrayUtils.isEquals(expected, actual);
            } else {
                equals = ObjectUtils.equals(expected, actual);
            }
        }
        return equals;
    }

    /**
     * Add an inclusion filter.
     * 
     * @param filter the filter
     * @return this instance for chaining
     */
    public BeanTester include(TypeFilter filter) {
        beanProvider.addIncludeFilter(filter);
        return this;
    }
    
    /**
     * Include all beans with a nullary constructor.
     * 
     * @return this instance for chaining
     */
    public BeanTester includeAllWithNullaryConstructor() {
        include(new HasNullaryConstructorFilter());
        return this;
    }

    /**
     * Add an exclusion filter.
     * 
     * @param filter the filter
     * @return this instance for chaining
     */
    public BeanTester exclude(TypeFilter filter) {
        beanProvider.addExcludeFilter(filter);
        return this;
    }

    /**
     * Excludes a property from testing.
     * 
     * @param declaringClass the declaring class
     * @param propertyName name of the property
     * @return this instance for chaining
     */
    public BeanTester excludeProperty(Class<?> declaringClass, String propertyName) {
        excludedProperties.add(new PropertyReference(declaringClass, propertyName));
        return this;
    }

    /**
     * If we should also test the parent properties.
     * 
     * @param inherit to inherit
     * @return this instance for chaining
     */
    public BeanTester inherit(boolean inherit) {
        this.inherit = inherit;
        return this;
    }

}
