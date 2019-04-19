package nl._42.beanie.convert;

import io.beanmapper.BeanMapper;

public class BeanMapperConverter implements BeanConverter {

  private final BeanMapper beanMapper;

  public BeanMapperConverter(BeanMapper beanMapper) {
    this.beanMapper = beanMapper;
  }

  @Override
  public <T> T convert(Object bean, Class<T> targetType) {
    return beanMapper.map(bean, targetType);
  }

}
