package nl._42.beanie.convert;

public class UnsupportedBeanConverter implements BeanConverter {

  @Override
  public <T> T convert(Object bean, Class<T> targetType) {
    throw new UnsupportedOperationException("Conversions are not supported");
  }

}
