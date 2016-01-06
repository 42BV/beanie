package io.dynamo.generator;

import java.lang.reflect.Method;

import org.springframework.util.ReflectionUtils;

public class FirstEnumValueGenerator implements ValueGenerator {

    public Object generate(Class<?> valueType) {
        Method valuesMethod = ReflectionUtils.findMethod(valueType, "values");
        Object result = ReflectionUtils.invokeMethod(valuesMethod, null);
        return ((Object[]) result)[0];
    }

}