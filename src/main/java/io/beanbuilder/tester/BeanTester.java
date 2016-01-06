package io.beanbuilder.tester;

import io.beanbuilder.BeanBuilder;
import io.beanbuilder.tester.strategy.ObjectEqualizer;
import io.beanbuilder.tester.strategy.SimpleObjectEqualizer;
import io.beanbuilder.util.Classes;
import io.beanbuilder.util.PropertyReference;

import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
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

    private final BeanBuilder beanBuilder;
    
    private final ObjectEqualizer equalizer;

    private boolean inherit = true;

    public BeanTester() {
        this(new BeanBuilder());
    }
    
    public BeanTester(BeanBuilder beanBuilder) {
        this(beanBuilder, new SimpleObjectEqualizer());
    }

    public BeanTester(BeanBuilder beanBuilder, ObjectEqualizer equalizer) {
        this.provider = new ClassPathScanningCandidateComponentProvider(false);
        this.beanBuilder = beanBuilder;
        this.equalizer = equalizer;
        
        // Exclude default property that have unusable getter and setters
        exclude(Throwable.class, "stackTrace");
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

        try {
            final BeanWrapper beanWrapper = newBeanWrapper(beanClass);
            verifyAllProperties(beanClass, beanWrapper);
        } catch (RuntimeException rte) {
            throw new AssertionError("Could not verify bean: " + beanClass.getSimpleName(), rte);
        }
    }

    private void verifyAllProperties(final Class<?> beanClass, final BeanWrapper beanWrapper) {
        for (PropertyDescriptor propertyDescriptor : beanWrapper.getPropertyDescriptors()) {
            if (isPropertyToVerify(beanClass, propertyDescriptor)) {
                verifyProperty(beanWrapper, propertyDescriptor);
            }
        }
    }

    private BeanWrapper newBeanWrapper(Class<?> beanClass) {
        Object bean = beanBuilder.generate(beanClass);
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
        return !excludedProperties.contains(propertyReference);
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
        Object generatedValue = beanBuilder.generate(propertyType);
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
        
        if (!equalizer.isEqual(value, result)) {
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
     * Include all beans.
     * 
     * @return this instance for chaining
     */
    public BeanTester includeAll() {
        include(new AssignableTypeFilter(Object.class));
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
     * Excludes a pattern from testing.
     * 
     * @param pattern the pattern
     * @return this instance for chaining
     */
    public BeanTester exclude(String pattern) {
        return exclude(new RegexPatternTypeFilter(Pattern.compile(pattern)));
    }
    
    /**
     * Excludes a class from testing.
     * 
     * @param beanClass the bean class
     * @return this instance for chaining
     */
    public BeanTester exclude(Class<?> beanClass) {
        return exclude(new AssignableTypeFilter(beanClass));
    }

    /**
     * Excludes a property from testing.
     * 
     * @param declaringClass the declaring class
     * @param propertyName name of the property
     * @return this instance for chaining
     */
    public BeanTester exclude(Class<?> declaringClass, String propertyName) {
        excludedProperties.add(new PropertyReference(declaringClass, propertyName));
        beanBuilder.skip(declaringClass, propertyName);
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
