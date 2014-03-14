/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package org.beanbuilder;

import static org.apache.commons.lang.StringUtils.substringAfter;
import static org.apache.commons.lang.StringUtils.uncapitalize;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.beanbuilder.BeanBuilder.ConfigurableBeanBuildCommand;
import org.springframework.aop.Advisor;

/**
 * Appends bean building logic to builders, allowing custom builder interfaces.
 * Whenever a custom method is invoked, such as <code>withId(1)</code> we will
 * automatically decorate the bean with an "id" property value of 1.
 * <p>
 * Providing no argument, such as <code>withId()</code>, we decorate the bean
 * with a generated "id" property value. The property value is generated using
 * the same bean builder.
 *
 * @author Jeroen van Schagen
 * @since Feb 14, 2014
 */
public final class CustomBeanBuilderAdvisor implements Advisor {
    
    private static final String WITH_PREFIX = "with";

    private final ConfigurableBeanBuildCommand<?> command;
    
    CustomBeanBuilderAdvisor(ConfigurableBeanBuildCommand<?> command) {
        this.command = command;
    }

    @Override
    public boolean isPerInstance() {
        return true;
    }
    
    @Override
    public Advice getAdvice() {
        return new CustomBeanBuilderAdvice();
    }
    
    private class CustomBeanBuilderAdvice implements MethodInterceptor {
        
        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            final String methodName = invocation.getMethod().getName();
            if (methodName.startsWith(WITH_PREFIX)) {
                String propertyName = substringAfter(methodName, WITH_PREFIX);
                propertyName = uncapitalize(propertyName);

                Object[] arguments = invocation.getArguments();
                if (arguments.length == 0) {
                    return command.withGeneratedValue(propertyName);
                } else {
                    return command.withValue(propertyName, arguments[0]);
                }
            } else {
                return invocation.proceed();
            }
        }

    }

}
