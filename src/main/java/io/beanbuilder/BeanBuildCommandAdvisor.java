/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package io.beanbuilder;

import io.beanbuilder.generator.ValueGenerator;

import java.lang.reflect.Method;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.Advisor;

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
public final class BeanBuildCommandAdvisor implements Advisor {
    
    private static final String WITH_PREFIX = "with";

    private final EditableBeanBuildCommand<?> command;
    
    public BeanBuildCommandAdvisor(EditableBeanBuildCommand<?> command) {
        this.command = command;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPerInstance() {
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Advice getAdvice() {
        return new CustomBeanBuilderAdvice();
    }
    
    private class CustomBeanBuilderAdvice implements MethodInterceptor {
        
        /**
         * {@inheritDoc}
         */
        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            final Method method = invocation.getMethod();
            final Object[] arguments = invocation.getArguments();
            final String preffix = getPreffix(method);

            if (isAdvicedMethod(method, arguments, preffix)) {
                return setPropertyValue(method, arguments, preffix);
            } else {
                return invocation.proceed();
            }
        }

        private String getPreffix(final Method method) {
            BeanBuildConfig config = method.getDeclaringClass().getAnnotation(BeanBuildConfig.class);
            return config != null ? config.preffix() : WITH_PREFIX;
        }

        private boolean isAdvicedMethod(final Method method, final Object[] arguments, final String preffix) {
            return method.getName().startsWith(preffix) && arguments.length <= 1;
        }
        
        private Object setPropertyValue(final Method method, final Object[] arguments, final String preffix) {
            String propertyName = getPropertyName(method, preffix);
            if (arguments.length == 1) {
                Object argument = arguments[0];
                if (argument instanceof ValueGenerator) {
                    return command.generateValue(propertyName, (ValueGenerator) argument);
                } else {
                    return command.withValue(propertyName, argument);
                }
            } else {
                return command.generateValue(propertyName);
            }
        }

        private String getPropertyName(final Method method, final String preffix) {
            String propertyName = method.getName().substring(preffix.length());
            return uncapitalize(propertyName);
        }

        private String uncapitalize(final String propertyName) {
            return propertyName.substring(0, 1).toLowerCase() + propertyName.substring(1);
        }

    }

}
