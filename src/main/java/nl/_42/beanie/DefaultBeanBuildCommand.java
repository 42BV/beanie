/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.beanie;

import io.beanmapper.BeanMapper;

import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import nl._42.beanie.generator.ValueGenerator;
import nl._42.beanie.util.PropertyReference;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.DirectFieldAccessor;
import org.springframework.beans.PropertyAccessor;

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

    private final BeanMapper beanMapper;

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

    public DefaultBeanBuildCommand(BeanBuilder beanBuilder, Class<T> type, BeanMapper beanMapper) {
        this.beanBuilder = beanBuilder;
        this.beanMapper = beanMapper;
        setBean(beanBuilder.getBeanGenerator().generate(type));
    }
    
    public DefaultBeanBuildCommand(BeanBuilder beanBuilder, Object bean, BeanMapper beanMapper) {
        this.beanBuilder = beanBuilder;
        this.beanMapper = beanMapper;
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
            setPropertyValue(beanWrapper, propertyName, value);
        } else {
            setPropertyValue(fieldAccessor, propertyName, value);
        }
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void setPropertyValue(PropertyAccessor propertyAccessor, String propertyName, Object value) {
        if (shouldBeAddedToCollection(propertyAccessor, propertyName, value)) {
            addValueToCollection(propertyAccessor, propertyName, value);
        } else {
            propertyAccessor.setPropertyValue(propertyName, value);
        }
    }
    
    private boolean shouldBeAddedToCollection(PropertyAccessor propertyAccessor, String propertyName, Object value) {
        Class<?> propertyType = propertyAccessor.getPropertyType(propertyName);
        if (propertyType == null) {
            throw new IllegalArgumentException("Unknown property '" + propertyName + "' in " + beanWrapper.getWrappedClass().getSimpleName());
        }
        boolean isCollection = Collection.class.isAssignableFrom(propertyType);
        return isCollection && value != null && !(value instanceof Collection);
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void addValueToCollection(PropertyAccessor propertyAccessor, String propertyName, Object value) {
        Collection collection = (Collection) propertyAccessor.getPropertyValue(propertyName);
        if (collection == null) {
            Class<?> propertyType = propertyAccessor.getPropertyType(propertyName);
            collection = (Collection) beanBuilder.generate(propertyType);
            propertyAccessor.setPropertyValue(propertyName, collection);
        }
        collection.add(value);
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
    public <M> EditableBeanBuildCommand<M> map(Class<M> targetType) {
        M mapped = constructAndMap(targetType);
        return new DefaultBeanBuildCommand<>(beanBuilder, mapped, beanMapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <I extends EditableBeanBuildCommand<M>, M> I map(Class<I> interfaceType, Class<M> targetType) {
        M mapped = constructAndMap(targetType);
        return beanBuilder.startAs(interfaceType, mapped);
    }

    private <M> M constructAndMap(Class<M> targetType) {
        return beanMapper.map(construct(), targetType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <I extends EditableBeanBuildCommand<B>, B> I as(Class<I> interfaceType) {
        return beanBuilder.startAs(interfaceType, (B) construct());
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
        return construct(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public T construct(boolean autoSave) {
        T bean = (T) beanWrapper.getWrappedInstance();
        if (!AopUtils.isAopProxy(bean)) {
            for (String propertyName : new HashSet<>(propertiesToGenerate)) {
                generateAndSetProperty(propertyName, autoSave);
            }
        }
        return bean;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public T save() {
        T bean = construct(true);
        return beanBuilder.save(bean);
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