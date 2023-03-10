package nl._42.beanie.spring;

import io.beanmapper.BeanMapper;
import nl._42.beanie.BeanBuilder;
import nl._42.beanie.convert.BeanConverter;
import nl._42.beanie.convert.BeanMapperConverter;
import nl._42.beanie.convert.UnsupportedBeanConverter;
import nl._42.beanie.save.BeanSaver;
import nl._42.beanie.save.JpaBeanSaver;
import nl._42.beanie.save.NoOperationBeanSaver;
import nl._42.beanie.save.TransactionalBeanSaver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Configuration
@ConditionalOnProperty(name = "beanie.enabled", havingValue = "true", matchIfMissing = true)
public class BeanieAutoConfiguration {

  @Autowired(required = false)
  private PlatformTransactionManager transactionManager;

  @Autowired(required = false)
  private BeanConverter beanConverter;

  @Autowired(required = false)
  private BeanSaver beanSaver;

  @Autowired(required = false)
  private List<BeanieConfigurer> configurers = new ArrayList<>();

  @Bean
  public BeanBuilder beanBuilder() {
    BeanConverter beanConverter = beanConverter();
    BeanSaver beanSaver = beanSaver();

    BeanBuilder beanBuilder = new BeanBuilder(beanConverter, beanSaver);
    configurers.forEach(configurer -> configurer.configure(beanBuilder));
    return beanBuilder;
  }

  private BeanConverter beanConverter() {
    return Optional.ofNullable(this.beanConverter).orElseGet(UnsupportedBeanConverter::new);
  }

  private BeanSaver beanSaver() {
    BeanSaver beanSaver = Optional.ofNullable(this.beanSaver).orElseGet(NoOperationBeanSaver::new);
    if (transactionManager != null) {
      beanSaver = new TransactionalBeanSaver(transactionManager, beanSaver);
    }
    return beanSaver;
  }

  @Configuration
  @ConditionalOnClass(EntityManager.class)
  public static class JpaBeanieAutoConfiguration {

    @Bean
    public BeanSaver beanSaver() {
      return new JpaBeanSaver();
    }

  }

  @Configuration
  @ConditionalOnBean(BeanMapper.class)
  public static class BeanMapperBeanieAutoConfiguration {

    @Bean
    public BeanConverter beanConverter(BeanMapper beanMapper) {
      return new BeanMapperConverter(beanMapper);
    }

  }

}
