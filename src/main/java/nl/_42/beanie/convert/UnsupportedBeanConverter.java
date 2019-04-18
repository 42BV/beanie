package nl._42.beanie.convert;

public class UnsupportedBeanConverter implements BeanConverter {

  @Override
  public <R> R convert(Object bean, Class<R> resultType) {
    throw new UnsupportedOperationException("Conversions are not supported");
  }

}
