package nl._42.beanie.convert;

/**
 * Responsible for convert beans to another type.
 */
public interface BeanConverter {

  /**
   * Convert the bean to another type.
   * @param bean the bean
   * @param targetType the target type
   * @param <T> the target type
   * @return the converted bean
   */
  <T> T convert(Object bean, Class<T> targetType);

}
