package nl._42.beanie;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.GenericTypeResolver;

public abstract class WrappedBeanBuilder<T, C extends BeanBuildCommand<T>> {
    
    private BeanBuilder beanBuilder;
    
    private final Class<C> interfaceType;

    /**
     * Create a new wrapped bean builder, using the type argument resolver.
     */
    @SuppressWarnings("unchecked")
    public WrappedBeanBuilder() {
        this.interfaceType = (Class<C>) GenericTypeResolver.resolveTypeArguments(getClass(), WrappedBeanBuilder.class)[1];
    }
    
    /**
     * Create a new wrapped bean builder.
     * @param interfaceType the interface type
     */
    public WrappedBeanBuilder(Class<C> interfaceType) {
        this.interfaceType = interfaceType;
    }
    
    /**
     * Start with building a bean.
     * @return the build command
     */
    public C start() {
        return beanBuilder.startAs(interfaceType);
    }
    
    /**
     * Start with building a bean, using this template.
     * @param bean the template bean
     * @return the build command
     */
    public C wrap(T bean) {
        return beanBuilder.startAs(interfaceType, bean);
    }
    
    /**
     * Store the bean builder to use. (autowired)
     * @param beanBuilder the bean builder
     */
    @Autowired
    public void setBeanBuilder(BeanBuilder beanBuilder) {
        this.beanBuilder = beanBuilder;
    }

}
