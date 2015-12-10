/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.beanbuilder;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configurations on a bean build command.
 *
 * @author Jeroen van Schagen
 * @since Jun 23, 2015
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface BeanBuildConfig {
    
    String preffix() default "with";

}
