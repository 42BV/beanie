package io.dynamo.util;

import org.springframework.aop.Advisor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.target.SingletonTargetSource;

public class Proxies {
    
    @SuppressWarnings("unchecked")
    public static <T> T wrapAsProxy(Class<?> interfaceType, T instance, Advisor advisor) {
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setTargetSource(new SingletonTargetSource(instance));
        proxyFactory.addInterface(interfaceType);
        proxyFactory.addAdvisor(advisor);
        return (T) proxyFactory.getProxy();
    }

}
