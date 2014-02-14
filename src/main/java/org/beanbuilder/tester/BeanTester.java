package org.beanbuilder.tester;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;
import org.beanbuilder.generate.TypeValueGenerator;
import org.beanbuilder.generate.ValueGenerator;
import org.beanbuilder.support.Classes;
import org.beanbuilder.support.PropertyReference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

/**
 * Verifies the getter and setter methods of beans. The retrieved
 * value should be semantically equal to the initially set value.
 * 
 * @author Jeroen van Schagen
 */
public class BeanTester {

    private final Logger logger = Logger.getLogger(getClass());

    private final ValueGenerator valueGenerator;

    private final Set<PropertyReference> propertiesToExclude = new HashSet<PropertyReference>();
    
    private boolean inherit = true;

    public BeanTester() {
        this(new TypeValueGenerator());
    }

    public BeanTester(ValueGenerator valueGenerator) {
        this.valueGenerator = valueGenerator;
        excludeProperty(Throwable.class, "stackTrace");
    }

    /**
     * Verify the getter and setters of each bean, with an empty constructor, in the specified base package.
     * 
     * @param basePackage the base package to search for beans
     */
    public int verifyBeans(String basePackage) {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new HasNullaryConstructorFilter());
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
     */
    public void verifyBean(Class<?> beanClass) {
        logger.debug("Verifying bean: " + beanClass.getName());
        final BeanWrapper beanWrapper = createBeanWrapper(beanClass);

        for (PropertyDescriptor propertyDescriptor : beanWrapper.getPropertyDescriptors()) {
            if (isAllowedProperty(beanClass, propertyDescriptor)) {
                verifyProperty(beanWrapper, propertyDescriptor);
            }
        }
    }
    
    private BeanWrapper createBeanWrapper(Class<?> beanClass) {
        Object bean = BeanUtils.instantiateClass(beanClass);
        return new BeanWrapperImpl(bean);
    }

    private boolean isAllowedProperty(Class<?> beanClass, PropertyDescriptor propertyDescriptor) {
        boolean verify = false;
        if (isReadWriteProperty(propertyDescriptor)) {
        	String propertyName = propertyDescriptor.getName();
        	Class<?> declaringClass = propertyDescriptor.getWriteMethod().getDeclaringClass();
        	verify = isDeclaredInBean(beanClass, declaringClass) && isNotExcluded(declaringClass, propertyName);
        }
        return verify;
    }

	private boolean isDeclaredInBean(Class<?> beanClass, Class<?> declaringClass) {
		return inherit || declaringClass.equals(beanClass);
	}

    private boolean isReadWriteProperty(PropertyDescriptor propertyDescriptor) {
        return propertyDescriptor.getReadMethod() != null && propertyDescriptor.getWriteMethod() != null;
    }
    
    private boolean isNotExcluded(Class<?> declaringClass, String propertyName) {
        PropertyReference propertyReference = new PropertyReference(declaringClass, propertyName);
		return ! propertiesToExclude.contains(propertyReference);
    }
    
    /**
     * Verify the getter and setter of a property.
     * 
     * @param beanClass the bean class
     * @param propertyName the property name
     */
    public void verifyProperty(Class<?> beanClass, String propertyName) {
        final BeanWrapper beanWrapper = createBeanWrapper(beanClass);
        verifyProperty(beanWrapper, beanWrapper.getPropertyDescriptor(propertyName));
    }

    private void verifyProperty(BeanWrapper beanWrapper, PropertyDescriptor propertyDescriptor) {
        final String propertyName = propertyDescriptor.getName();
        final Class<?> propertyType = propertyDescriptor.getPropertyType();
        
        logger.debug("Verifying property '" + propertyName + "' of bean: " + beanWrapper.getWrappedClass().getName());

        try {
            if (isNullAllowed(propertyType)) {
                checkPropertyWithValue(beanWrapper, propertyName, null);
            }
            
            Object generatedValue = valueGenerator.generate(propertyType);
            checkPropertyWithValue(beanWrapper, propertyName, generatedValue);
        } catch(InconsistentGetterAndSetterException igse) {
        	throw igse;
        } catch (RuntimeException rte) {
            String message = String.format(
                    "Property '%s' of '%s' has an unusable getter and/or setter.",
                    propertyName, beanWrapper.getWrappedClass().getName());
            throw new IllegalStateException(message, rte);
        }
    }

	private boolean isNullAllowed(final Class<?> propertyType) {
		return ! propertyType.isPrimitive();
	}

    private void checkPropertyWithValue(BeanWrapper beanWrapper, String propertyName, Object value) {
        beanWrapper.setPropertyValue(propertyName, value);
        Object retrievedValue = beanWrapper.getPropertyValue(propertyName);

        if (! isEqual(value, retrievedValue)) {
            String message = String.format(
                    "Property '%s' of '%s' returned a different value than initially set (original: %s, actual: %s).",
                    propertyName, beanWrapper.getWrappedClass().getName(), value, retrievedValue);
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
     * Excludes a property from testing.
     * 
     * @param declaringClass the declaring class
     * @param propertyName name of the property
     */
    public void excludeProperty(Class<?> declaringClass, String propertyName) {
        propertiesToExclude.add(new PropertyReference(declaringClass, propertyName));
    }

    public void setInherit(boolean inherit) {
        this.inherit = inherit;
    }

    /**
     * Only matches classes with a no argument constructor.
     */
    private static class HasNullaryConstructorFilter implements TypeFilter {

        public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
            ClassMetadata metadata = metadataReader.getClassMetadata();
            Class<?> clazz = Classes.forName(metadata.getClassName());
            return Classes.hasNullaryConstructor(clazz);
        }

    }

}
