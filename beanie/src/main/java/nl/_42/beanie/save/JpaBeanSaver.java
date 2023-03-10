/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.beanie.save;

import org.springframework.core.annotation.AnnotationUtils;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Persists entities in the entity manager. Non entity
 * values are ignored and just returned without actions.
 *
 * @author Jeroen van Schagen
 * @since Mar 10, 2014
 */
public class JpaBeanSaver implements BeanSaver {
    
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T save(final T bean) {
        if (isEntity(bean)) {
            entityManager.persist(bean);
        }
        return bean;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(Object bean) {
        if (isEntity(bean)) {
            entityManager.remove(bean);
        }
    }

    private boolean isEntity(Object value) {
        if (value == null) {
            return false;
        }

        return AnnotationUtils.findAnnotation(value.getClass(), Entity.class) != null;
    }
    
}
