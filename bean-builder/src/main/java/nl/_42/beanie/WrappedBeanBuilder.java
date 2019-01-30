package nl._42.beanie;

import org.springframework.core.GenericTypeResolver;

/**
 * Build builder specific to one type of build command.
 *
 * @param <T> the bean type
 * @param <C> the command interface type
 */
public class WrappedBeanBuilder<T, C extends BeanBuildCommand<T>> {
    
    private final BeanBuilder beanBuilder;
    
    private final Class<C> interfaceType;

    /**
     * Create a new wrapped bean builder, using the type argument resolver.
     *
     * @param beanBuilder the bean builder
     */
    @SuppressWarnings("unchecked")
    public WrappedBeanBuilder(BeanBuilder beanBuilder) {
        this.beanBuilder = beanBuilder;
        this.interfaceType = (Class<C>) GenericTypeResolver.resolveTypeArguments(getClass(), WrappedBeanBuilder.class)[1];
    }
    
    /**
     * Create a new wrapped bean builder.
     *
     * @param beanBuilder the bean builder
     * @param interfaceType the interface type
     */
    public WrappedBeanBuilder(BeanBuilder beanBuilder, Class<C> interfaceType) {
        this.beanBuilder = beanBuilder;
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

}
