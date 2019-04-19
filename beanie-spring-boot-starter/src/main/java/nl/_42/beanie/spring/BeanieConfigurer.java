package nl._42.beanie.spring;

import nl._42.beanie.BeanBuilder;

/**
 * Capable of customizing the registered bean builder.
 */
public interface BeanieConfigurer {

  /**
   * Configure the registered bean builder.
   * @param beanBuilder the bean builder
   */
  void configure(BeanBuilder beanBuilder);

}
