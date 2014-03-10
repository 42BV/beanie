/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package org.beanbuilder.save;

import javax.persistence.Entity;
import javax.persistence.EntityManager;

import org.springframework.core.annotation.AnnotationUtils;

/**
 * Persists entities in the entity manager. Non entity
 * values are ignored and just returned without actions.
 *
 * @author Jeroen van Schagen
 * @since Mar 10, 2014
 */
public class JpaEntitySaver implements ValueSaver {
    
    private final EntityManager entityManager;
    
    public JpaEntitySaver(EntityManager entityManager) {
        super();
        this.entityManager = entityManager;
    }

    @Override
    public <T> T save(T value) {
        if (value != null && isEntity(value)) {
            entityManager.persist(value);
        }
        return value;
    }

    protected boolean isEntity(Object value) {
        return AnnotationUtils.findAnnotation(value.getClass(), Entity.class) != null;
    }
    
}
