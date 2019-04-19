package nl._42.beanie.spring;

import io.beanmapper.BeanMapper;
import io.beanmapper.config.BeanMapperBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperConfiguration {

  @Bean
  public BeanMapper beanMapper() {
    return new BeanMapperBuilder().setApplyStrictMappingConvention(false).build();
  }

}
