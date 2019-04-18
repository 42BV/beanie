package nl._42.beanie.convert;

public interface BeanConverter {

  <R> R convert(Object bean, Class<R> resultType);

}
