package org.beanbuilder.generate;

public class ConstantValueGenerator implements ValueGenerator {

    private final Object value;

    public ConstantValueGenerator(Object value) {
        this.value = value;
    }

    public Object generate(Class<?> valueType) {
        return value;
    }

}