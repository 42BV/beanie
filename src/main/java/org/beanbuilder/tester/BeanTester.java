package org.beanbuilder.tester;

import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.beanbuilder.BeanBuilder;
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
    
    private final ClassPathScanningCandidateComponentProvider provider;

    private final BeanBuilder builder;
    
    private final ObjectEqualizer equalizer;

    private boolean inherit = true;

    public BeanTester() {
        this(new BeanBuilder(), new ObjectEqualizer());
    }

    public BeanTester(BeanBuilder builder, ObjectEqualizer equalizer) {
        this.provider = new ClassPathScanningCandidateComponentProvider(false);
        this.builder = builder;
        this.equalizer = equalizer;
        
        // Exclude default property that have unusable getter and setters
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
        Set<BeanDefinition> beanDefinitions = provider.findCandidateComponents(basePackage);
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
        Object bean = builder.generate(beanClass);
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
        Object generatedValue = builder.generate(propertyType);
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
        
        if (! equalizer.isEqual(value, result)) {
            String message = String.format(
                    "Property '%s' of '%s' returned a different value than initially set (original: %s, actual: %s).",
                    propertyName, beanWrapper.getWrappedClass().getName(), value, result);
            throw new InconsistentGetterAndSetterException(message);
        }
    }

    /**
     * Add an inclusion filter.
     * 
     * @param filter the filter
     * @return this instance for chaining
     */
    public BeanTester include(TypeFilter filter) {
        provider.addIncludeFilter(filter);
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
        provider.addExcludeFilter(filter);
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
        builder.skip(declaringClass, propertyName);
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
