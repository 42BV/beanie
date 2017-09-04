package nl._42.beanie;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.GenericTypeResolver;

public abstract class BeanBuilderFacade<T, C extends BeanBuildCommand<T>> {
    
    private BeanBuilder beanBuilder;
    
    private final Class<C> interfaceType;

    @SuppressWarnings("unchecked")
    public BeanBuilderFacade() {
        this.interfaceType = (Class<C>) GenericTypeResolver.resolveTypeArguments(getClass(), BeanBuilderFacade.class)[1];
    }
    
    public BeanBuilderFacade(Class<C> interfaceType) {
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
