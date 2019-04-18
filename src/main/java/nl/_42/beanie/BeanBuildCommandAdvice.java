/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.beanie;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import nl._42.beanie.generator.ValueGenerator;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * Appends bean building logic to builders, allowing custom builder interfaces.
 * Whenever a custom method is invoked, such as <code>withName("henk")</code> we will
 * automatically decorate the bean with an "name" property value of "henk".
 * <p>
 * Providing no argument, such as <code>withName()</code>, we decorate the bean
 * with a generated "name" property value. The property value is generated using
 * the same bean builder.
 *
 * @author Jeroen van Schagen
 * @since Feb 14, 2014
 */
public final class BeanBuildCommandAdvice implements MethodInterceptor {

    private final EditableBeanBuildCommand<?> command;
        
    private final String preffix;
    
    private Object proxy;

    public BeanBuildCommandAdvice(EditableBeanBuildCommand<?> command, String preffix) {
        this.command = command;
        this.preffix = preffix;
    }
    
    /**
     * @param proxy the proxy to set
     */
    public void setProxy(Object proxy) {
        this.proxy = proxy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        final Method method = invocation.getMethod();
        final Object[] args = invocation.getArguments();
        
        if (method.isDefault()) {
            MethodHandle handle = getMethodHandle(method);
            return handle.bindTo(proxy).invokeWithArguments(args);
        } else {
            String propertyName = getPropertyName(method, preffix);
            if (args.length == 1) {
                Object argument = args[0];
                if (argument instanceof ValueGenerator) {
                    return command.generateValue(propertyName, (ValueGenerator) argument);
                } else {
                    return command.withValue(propertyName, argument);
                }
            } else {
                return command.generateValue(propertyName);
            }
        }
    }

    private String getPropertyName(final Method method, final String preffix) {
        String propertyName = method.getName().substring(preffix.length());
        return uncapitalize(propertyName);
    }

    private String uncapitalize(final String propertyName) {
        return propertyName.substring(0, 1).toLowerCase() + propertyName.substring(1);
    }
    
    private static MethodHandle getMethodHandle(Method method) {
        final Class<?> declaringClass = method.getDeclaringClass();
        
        try {
            Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
            constructor.setAccessible(true);
            
            return constructor.newInstance(declaringClass, MethodHandles.Lookup.PRIVATE).unreflectSpecial(method, declaringClass);
        } catch (IllegalAccessException | NoSuchMethodException | InstantiationException | InvocationTargetException e) {
            throw new IllegalStateException("Could not retrieve method handle.", e);
        }
    }

}
