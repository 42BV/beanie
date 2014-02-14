/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package org.beanbuilder;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.StringUtils;
import org.beanbuilder.BeanBuilder.ConfigurableBeanBuildCommand;
import org.springframework.aop.Advisor;

/**
 * Advisor to support custom bean builders. 
 *
 * @author jeroen
 * @since Feb 14, 2014
 */
public class CustomBeanBuilderAdvisor implements Advisor {
    
    private final ConfigurableBeanBuildCommand<?> command;
    
    public CustomBeanBuilderAdvisor(ConfigurableBeanBuildCommand<?> command) {
        this.command = command;
    }

    @Override
    public boolean isPerInstance() {
        return true;
    }
    
    @Override
    public Advice getAdvice() {
        return new MethodInterceptor() {
            
            @Override
            public Object invoke(MethodInvocation invocation) throws Throwable {
                String methodName = invocation.getMethod().getName();
                if (methodName.startsWith("with")) {
                    String propertyName = StringUtils.substringAfter(methodName, "with");
                    return command.withValue(propertyName, invocation.getArguments()[0]);
                } else {
                    return invocation.proceed();
                }
            }
            
        };
    }

}
