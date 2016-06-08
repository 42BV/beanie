/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.dynamo;

import io.dynamo.generator.ValueGenerator;
import io.dynamo.util.PropertyReference;

import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.DirectFieldAccessor;

/**
 * Default implementation of the bean build command.
 *
 * @author Jeroen van Schagen
 * @since Feb 14, 2014
 */
class DefaultBeanBuildCommand<T> implements EditableBeanBuildCommand<T> {

    /**
     * Collection of all properties already touched.
     * We store this to only generate values for untouched properties.
     */
    private final Set<String> touchedProperties = new HashSet<>();
    
    /**
     * Collection of all properties we want to generate values.
     */
    private final Set<String> propertiesToGenerate = new HashSet<>();
    
    /**
     * Reference to the bean builder, generates beans and other values.
     */
    private final BeanBuilder beanBuilder;

    /**
     * Bean wrapper that holds a reference to the result bean.
     */
    private BeanWrapper beanWrapper;

    /**
     * Field accessor that holds a reference to the same result bean.
     * We need both a field accessor and bean wrapper to modify property
     * values that have no getter and setter.
     */
    private DirectFieldAccessor fieldAccessor;

    public DefaultBeanBuildCommand(BeanBuilder beanBuilder, Class<T> type) {
        this.beanBuilder = beanBuilder;
        setBean(beanBuilder.getBeanGenerator().generate(type));
    }
    
    public DefaultBeanBuildCommand(BeanBuilder beanBuilder, Object bean) {
        this.beanBuilder = beanBuilder;
        setBean(bean);
        markNotNullAsTouched();
    }

    private void markNotNullAsTouched() {
        for (PropertyDescriptor descriptor : beanWrapper.getPropertyDescriptors()) {
            final String propertyName = descriptor.getName();
            if (beanWrapper.isReadableProperty(propertyName) && beanWrapper.getPropertyValue(propertyName) != null) {
                markAsTouched(propertyName);
            }
        }
    }
    
    private final void setBean(Object bean) {
        this.beanWrapper = new BeanWrapperImpl(bean);
        this.fieldAccessor = new DirectFieldAccessor(bean);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EditableBeanBuildCommand<T> withValue(String propertyName, Object value) {
        setPropertyValue(propertyName, value);
        markAsTouched(propertyName);
        return this;
    }

    private void markAsTouched(String propertyName) {
        touchedProperties.add(propertyName);
        propertiesToGenerate.remove(propertyName);
    }
    
    private void setPropertyValue(String propertyName, Object value) {
        if (beanWrapper.isWritableProperty(propertyName)) {
            beanWrapper.setPropertyValue(propertyName, value);
        } else {
            fieldAccessor.setPropertyValue(propertyName, value);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public EditableBeanBuildCommand<T> load(Object source, String... exclusionArgs) {
        final Collection<String> exclusions = Arrays.asList(exclusionArgs);

        BeanWrapper sourceWrapper = new BeanWrapperImpl(source);
        for (PropertyDescriptor descriptor : sourceWrapper.getPropertyDescriptors()) {
            final String propertyName = descriptor.getName();
            if (sourceWrapper.isReadableProperty(propertyName) && beanWrapper.isWritableProperty(propertyName) && !isSkipped(propertyName, exclusions)) {
                withValue(propertyName, sourceWrapper.getPropertyValue(propertyName));
            }
        }
        return this;
    }

    private boolean isSkipped(final String propertyName, final Collection<String> exclusions) {
        PropertyDescriptor descriptor = beanWrapper.getPropertyDescriptor(propertyName);
        return exclusions.contains(propertyName) || beanBuilder.getSkippedProperties().contains(new PropertyReference(descriptor));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public EditableBeanBuildCommand<T> map(Function<T, T> function) {
        T result = function.apply(construct());
        setBean(result);
        return this;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public EditableBeanBuildCommand<T> doWith(Consumer<T> consumer) {
        T bean = construct();
        consumer.accept(bean);
        setBean(bean);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EditableBeanBuildCommand<T> generateValue(String propertyName, ValueGenerator generator) {
        PropertyDescriptor descriptor = beanWrapper.getPropertyDescriptor(propertyName);
        Object value = generator.generate(descriptor.getPropertyType());
        return this.withValue(propertyName, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EditableBeanBuildCommand<T> generateValue(String... propertyNames) {
        for (String propertyName : propertyNames) {
            touchedProperties.add(propertyName);
            propertiesToGenerate.add(propertyName);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EditableBeanBuildCommand<T> fill() {
        for (PropertyDescriptor descriptor : beanWrapper.getPropertyDescriptors()) {
            String propertyName = descriptor.getName();
            if (beanWrapper.isWritableProperty(propertyName) && !touchedProperties.contains(propertyName)) {
                if (!beanBuilder.getSkippedProperties().contains(new PropertyReference(descriptor))) {
                    generateValue(propertyName);
                }
            }
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T construct() {
        return finishBean(false);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public T save() {
        T bean = finishBean(true);
        return beanBuilder.save(bean);
    }
    
    @SuppressWarnings("unchecked")
    private T finishBean(boolean autoSave) {
        T bean = (T) beanWrapper.getWrappedInstance();
        if (!AopUtils.isAopProxy(bean)) {
            for (String propertyName : new HashSet<>(propertiesToGenerate)) {
                generateAndSetProperty(propertyName, autoSave);
            }
        }
        return bean;
    }

    private void generateAndSetProperty(String propertyName, boolean autoSave) {
        PropertyDescriptor descriptor = beanWrapper.getPropertyDescriptor(propertyName);
        Object value = beanBuilder.generateValue(beanWrapper.getWrappedClass(), descriptor);
        if (autoSave) {
            value = beanBuilder.save(value);
        }
        withValue(propertyName, value);
    }

}