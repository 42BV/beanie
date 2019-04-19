/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.beanie.save;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Persists entities in the entity manager. Non entity
 * values are ignored and just returned without actions.
 *
 * @author Jeroen van Schagen
 * @since Mar 10, 2014
 */
public class TransactionalBeanSaver implements BeanSaver {
    
    private final TransactionTemplate transactionTemplate;
    
    private final BeanSaver delegate;
    
    public TransactionalBeanSaver(PlatformTransactionManager transactionManager, BeanSaver delegate) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.delegate = delegate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T save(final T bean) {
        return transactionTemplate.execute(status -> delegate.save(bean));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(final Object bean) {
        transactionTemplate.execute(status -> {
            delegate.delete(bean);
            return null;
        });
    }
    
}
