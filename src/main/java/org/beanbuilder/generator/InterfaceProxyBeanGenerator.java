/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package org.beanbuilder.generator;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.target.SingletonTargetSource;

/**
 * Creates a proxy of an interface that supports simple get and set behaviour.
 *
 * @author Jeroen van Schagen
 * @since Mar 26, 2015
 */
public class InterfaceProxyBeanGenerator implements ValueGenerator {
    
    @Override
    public Object generate(Class<?> interfaceClass) {
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setTargetSource(new SingletonTargetSource(new EmptyTargetSource()));
        proxyFactory.addInterface(interfaceClass);
        return proxyFactory.getProxy();
    }

    private static class EmptyTargetSource {
        // No logic needed, just a filler
    }

}
