package nl._42.beanie;

import nl._42.beanie.domain.SimpleBean;
import org.springframework.stereotype.Component;

@Component
public class SimpleBeanBuilder extends WrappedBeanBuilder<SimpleBean, SimpleBeanBuildCommand> {

    public SimpleBeanBuilder(BeanBuilder beanBuilder) {
        super(beanBuilder);
    }

}
