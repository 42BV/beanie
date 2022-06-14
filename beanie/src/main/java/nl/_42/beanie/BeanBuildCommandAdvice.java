/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.beanie;

import nl._42.beanie.compatibility.Methods;
import nl._42.beanie.generator.ValueGenerator;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;

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
        
    private final String prefix;
    
    private Object proxy;

    public BeanBuildCommandAdvice(EditableBeanBuildCommand<?> command, String prefix) {
        this.command = command;
        this.prefix = prefix;
    }
    
    /**
     * Binds this advice to a proxy.
     *
     * @param proxy the new proxy
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
            MethodHandle handle = Methods.getMethodHandle(method);
            return handle.bindTo(proxy).invokeWithArguments(args);
        } else {
            String propertyName = getPropertyName(method, prefix);
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

    private String getPropertyName(Method method, String prefix) {
        String propertyName = method.getName().substring(prefix.length());
        return lowerCaseFirstCharacter(propertyName);
    }

    private String lowerCaseFirstCharacter(String propertyName) {
        return propertyName.substring(0, 1).toLowerCase() + propertyName.substring(1);
    }

}
