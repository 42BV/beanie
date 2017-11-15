package nl._42.beanie;

import org.springframework.core.GenericTypeResolver;

/**
 * Bean builder specialized on a single build command.
 *
 * @author Jeroen van Schagen
 * @since Nov 15, 2017
 */
public class WrappedBeanBuilder<T, C extends BeanBuildCommand<T>> {
    
    private final BeanBuilder beanBuilder;
    
    private final Class<C> interfaceType;

    /**
     * Create a new wrapped bean builder, using the type argument resolver.
     * @param beanBuilder the bean builder
     */
    @SuppressWarnings("unchecked")
    protected WrappedBeanBuilder(BeanBuilder beanBuilder) {
        this.interfaceType = (Class<C>) GenericTypeResolver.resolveTypeArguments(getClass(), WrappedBeanBuilder.class)[1];
        this.beanBuilder = beanBuilder;
    }
    
    /**
     * Create a new wrapped bean builder.
     * @param interfaceType the interface type
     * @param beanBuilder the bean builder
     */
    public WrappedBeanBuilder(Class<C> interfaceType, BeanBuilder beanBuilder) {
        this.interfaceType = interfaceType;
        this.beanBuilder = beanBuilder;
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

}
