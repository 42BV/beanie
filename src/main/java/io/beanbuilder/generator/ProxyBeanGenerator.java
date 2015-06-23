/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.beanbuilder.generator;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.target.SingletonTargetSource;

/**
 * Creates a proxy of an interface that supports simple get and set behaviour.
 *
 * @author Jeroen van Schagen
 * @since Mar 26, 2015
 */
public class ProxyBeanGenerator implements ValueGenerator {
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object generate(Class<?> beanClass) {
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setTargetSource(new SingletonTargetSource(new EmptyTargetSource()));
        if (beanClass.isInterface()) {
            proxyFactory.addInterface(beanClass);
        } else {
            proxyFactory.setTargetClass(beanClass);
        }
        return proxyFactory.getProxy();
    }

    private static class EmptyTargetSource {
        // No logic needed, just a filler
    }

}
