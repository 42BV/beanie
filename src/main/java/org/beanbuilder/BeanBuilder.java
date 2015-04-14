/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package org.beanbuilder;

import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.beanbuilder.generator.BeanGenerator;
import org.beanbuilder.generator.ConfigurableValueGenerator;
import org.beanbuilder.generator.ConstantValueGenerator;
import org.beanbuilder.generator.DefaultValueGenerator;
import org.beanbuilder.generator.ValueGenerator;
import org.beanbuilder.save.BeanSaver;
import org.beanbuilder.save.UnsupportedBeanSaver;
import org.beanbuilder.support.PropertyReference;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.target.SingletonTargetSource;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.DirectFieldAccessor;
import org.springframework.core.GenericTypeResolver;


/**
 * Builds new bean instances.
 *
 * @author Jeroen van Schagen
 * @since Feb 14, 2014
 */
public class BeanBuilder implements ValueGenerator {
    
    /**
     * Collection of properties that should be skipped.
     */
    private final Set<PropertyReference> skippedProperties = new HashSet<>();
    
    /**
     * Property specific value generators.
     */
    private final Map<PropertyReference, ValueGenerator> propertyGenerators = new HashMap<>();
    
    /**
     * Type specific value generators.
     */
    private final ConfigurableValueGenerator typeGenerator;
    
    /**
     * Generator used to generate the result beans.
     */
    private final BeanGenerator beanGenerator;
    
    /**
     * Saves the generated beans.
     */
    private final BeanSaver beanSaver;

    /**
     * Construct a new {@link BeanBuilder}.
     * <br><br>
     * <b>Note that using this constructor means the beans cannot be saved</b>
     */
    public BeanBuilder() {
        this(new UnsupportedBeanSaver());
    }

    /**
     * Construct a new {@link BeanBuilder}.
     * 
     * @param beanSaver responsible for saving the bean after creation
     */
    public BeanBuilder(BeanSaver beanSaver) {
        this.typeGenerator = new DefaultValueGenerator(this);
        this.beanGenerator = new BeanGenerator(this);
        this.beanSaver = beanSaver;
    }

    /**
     * Start building a new bean.
     * 
     * @param beanClass the type of bean to start building
     * @return the bean build command
     */
    public <T> EditableBuildCommand<T> newBean(Class<T> beanClass) {
        return new DefaultBeanBuildCommand<T>(this, beanClass);
    }
    
    /**
     * Start building a new bean, using a custom builder interface.
     * 
     * @param type the type of bean to start building
     * @param commandType the build command interface
     * @return the builder instance, capable of building beans
     */
    @SuppressWarnings("unchecked")
    public <T extends BuildCommand<?>> T newBeanBy(Class<T> commandType) {
        final Class<?> beanClass = GenericTypeResolver.resolveTypeArguments(commandType, BuildCommand.class)[0];
        final EditableBuildCommand<?> command = newBean(beanClass);

        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setTargetSource(new SingletonTargetSource(command));
        proxyFactory.addInterface(commandType);
        proxyFactory.addAdvisor(new CustomBeanBuilderAdvisor(command));
        return (T) proxyFactory.getProxy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object generate(Class<?> beanClass) {
        if (typeGenerator.contains(beanClass)) {
            return typeGenerator.generate(beanClass);
        }
        return newBean(beanClass).fill().build();
    }

    /**
     * Generate a new bean, properly casted to the correct type.
     * 
     * @param beanClass the bean class
     * @return the generated bean
     */
    @SuppressWarnings("unchecked")
    public <T> T generateSafely(Class<T> beanClass) {
        return (T) generate(beanClass);
    }

    private Object generateValue(Class<?> beanClass, PropertyDescriptor descriptor) {
        ValueGenerator generator = getGenerator(beanClass, descriptor);
        try {
            return generator.generate(descriptor.getPropertyType());
        } catch (RuntimeException rte) {
            throw new IllegalStateException("Could not generate property '" + descriptor.getName() + "' for: " + beanClass.getName(), rte);
        }
    }

    private ValueGenerator getGenerator(Class<?> beanClass, PropertyDescriptor descriptor) {
        ValueGenerator generator = this;
        PropertyReference reference = new PropertyReference(beanClass, descriptor.getName());
        if (propertyGenerators.containsKey(reference)) {
            generator = propertyGenerators.get(reference);
        } else if (typeGenerator.contains(descriptor.getPropertyType())) {
            generator = typeGenerator;
        }
        return generator;
    }

    /**
     * Skip a property from being generated.
     * 
     * @param declaringClass the bean that declares this property
     * @param propertyName the property name
     * @return this instance
     */
    public BeanBuilder skip(Class<?> declaringClass, String propertyName) {
        skippedProperties.add(new PropertyReference(declaringClass, propertyName));
        return this;
    }

    /**
     * Register a value generation strategy for a specific property reference.
     * 
     * @param declaringClass the bean class that declares our property
     * @param propertyName the name of the property
     * @param generator the generation strategy
     * @return this instance
     */
    public BeanBuilder register(Class<?> declaringClass, String propertyName, ValueGenerator generator) {
        propertyGenerators.put(new PropertyReference(declaringClass, propertyName), generator);
        return this;
    }
    
    /**
     * Register a constant value for a specific property reference.
     * 
     * @param declaringClass the bean class that declares our property
     * @param propertyName the name of the property
     * @param value the value to return
     * @return this instance
     */
    public BeanBuilder registerValue(Class<?> declaringClass, String propertyName, Object value) {
        return register(declaringClass, propertyName, new ConstantValueGenerator(value));
    }

    /**
     * Register a value generation strategy for a specific type.
     * 
     * @param valueType the type of value
     * @param generator the generation strategy
     * @return this instance
     */
    public BeanBuilder register(Class<?> valueType, ValueGenerator generator) {
        typeGenerator.register(valueType, generator);
        return this;
    }
    
    /**
     * Register a constant value for a specific type.
     * 
     * @param valueType the type of value
     * @param value the value to return
     * @return this instance
     */
    public BeanBuilder registerValue(Class<?> valueType, Object value) {
        return register(valueType, new ConstantValueGenerator(value));
    }

    /**
     * Saves the bean.
     * 
     * @param bean the bean to save
     * @return the saved bean
     */
    public <R> R save(R bean) {
        return beanSaver.save(bean);
    }

    /**
     * Deletes the bean.
     * 
     * @param bean the bean to delete
     */
    public void delete(Object bean) {
        beanSaver.delete(bean);
    }
    
    /**
     * Deletes multiple beans.
     * 
     * @param beans the beans to delete
     */
    public void deleteAll(Iterable<? extends Object> beans) {
        for (Object bean : beans) {
            beanSaver.delete(bean);
        }
    }
    
    /**
     * Retrieves the underlying bean generator.
     * 
     * @return the bean generator
     */
    public final BeanGenerator getBeanGenerator() {
        return beanGenerator;
    }

    /**
     * Command for building beans.
     *
     * @author Jeroen van Schagen
     * @since Feb 14, 2014
     */
    public interface BuildCommand<T> {
        
        /**
         * Generate all untouched, changable, values in our bean.
         * 
         * @return this instance, for chaining
         */
        BuildCommand<T> fill();
        
        /**
         * Build the new bean.
         * 
         * @return the created bean
         */
        T build();
        
        /**
         * Build and save new bean.
         * 
         * @return the saved bean
         */
        T save();

    }
    
    /**
     * Bean build command that allows users to declare custom property values.
     *
     * @author Jeroen van Schagen
     * @since Feb 14, 2014
     */
    public interface EditableBuildCommand<T> extends BuildCommand<T> {

        /**
         * Generate a value in our to be generated bean.
         * 
         * @param propertyName the property name
         * @return this instance, for chaining
         */
        EditableBuildCommand<T> generateValue(String propertyName);
        
        /**
         * Generate a value in our to be generated bean.
         * 
         * @param propertyName the property name
         * @param generator the value generator
         * @return this instance, for chaining
         */
        EditableBuildCommand<T> generateValue(String propertyName, ValueGenerator generator);
        
        /**
         * Declare a value in our to be generated bean.
         * 
         * @param propertyName the property name
         * @param value the property value
         * @return this instance, for chaining
         */
        EditableBuildCommand<T> setValue(String propertyName, Object value);
        
        /**
         * Copies all usable property values from a bean
         * into our result.
         * 
         * @param bean the bean to copy properties from
         * @param exclusions the property names to exclude from copy
         * @return this instance, for chaining
         */
        EditableBuildCommand<T> copyAllValuesFrom(Object bean, String... exclusions);

    }

    /**
     * Default implementation of the bean build command.
     *
     * @author Jeroen van Schagen
     * @since Feb 14, 2014
     */
    private static class DefaultBeanBuildCommand<T> implements EditableBuildCommand<T> {

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
        private final BeanWrapper beanWrapper;

        /**
         * Field accessor that holds a reference to the same result bean.
         * We need both a field accessor and bean wrapper to modify property
         * values that have no getter and setter.
         */
        private final DirectFieldAccessor fieldAccessor;

        public DefaultBeanBuildCommand(BeanBuilder beanBuilder, Class<T> type) {
            this.beanBuilder = beanBuilder;

            Object bean = beanBuilder.beanGenerator.generate(type);
            beanWrapper = new BeanWrapperImpl(bean);
            fieldAccessor = new DirectFieldAccessor(bean);
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public EditableBuildCommand<T> setValue(String propertyName, Object value) {
            setPropertyValue(propertyName, value);
            touchedProperties.add(propertyName);
            propertiesToGenerate.remove(propertyName);
            return this;
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
        public EditableBuildCommand<T> copyAllValuesFrom(Object source, String... exclusionArgs) {
            final Collection<String> exclusions = Arrays.asList(exclusionArgs);

            BeanWrapper sourceWrapper = new BeanWrapperImpl(source);
            for (PropertyDescriptor descriptor : sourceWrapper.getPropertyDescriptors()) {
                final String propertyName = descriptor.getName();
                if (sourceWrapper.isReadableProperty(propertyName) && beanWrapper.isWritableProperty(propertyName) && !isSkipped(propertyName, exclusions)) {
                    setValue(propertyName, sourceWrapper.getPropertyValue(propertyName));
                }
            }
            return this;
        }

        private boolean isSkipped(final String propertyName, final Collection<String> exclusions) {
            PropertyDescriptor descriptor = beanWrapper.getPropertyDescriptor(propertyName);
            return exclusions.contains(propertyName) || beanBuilder.skippedProperties.contains(new PropertyReference(descriptor));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public EditableBuildCommand<T> generateValue(String propertyName, ValueGenerator generator) {
            PropertyDescriptor descriptor = beanWrapper.getPropertyDescriptor(propertyName);
            Object value = generator.generate(descriptor.getPropertyType());
            return this.setValue(propertyName, value);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public EditableBuildCommand<T> generateValue(String propertyName) {
            touchedProperties.add(propertyName);
            propertiesToGenerate.add(propertyName);
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public EditableBuildCommand<T> fill() {
            for (PropertyDescriptor descriptor : beanWrapper.getPropertyDescriptors()) {
                String propertyName = descriptor.getName();
                if (beanWrapper.isWritableProperty(propertyName) && !touchedProperties.contains(propertyName)) {
                    if (!beanBuilder.skippedProperties.contains(new PropertyReference(descriptor))) {
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
        public T build() {
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
            setValue(propertyName, value);
        }

    }

}
