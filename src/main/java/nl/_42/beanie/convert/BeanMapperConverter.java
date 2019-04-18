package nl._42.beanie.convert;

import io.beanmapper.BeanMapper;
import io.beanmapper.config.BeanMapperBuilder;

public class BeanMapperConverter implements BeanConverter {

  private final BeanMapper beanMapper;

  public BeanMapperConverter() {
    this(new BeanMapperBuilder().setApplyStrictMappingConvention(false).build());
  }

  public BeanMapperConverter(BeanMapper beanMapper) {
    this.beanMapper = beanMapper;
  }

  @Override
  public <R> R convert(Object bean, Class<R> resultType) {
    return beanMapper.map(bean, resultType);
  }

}
