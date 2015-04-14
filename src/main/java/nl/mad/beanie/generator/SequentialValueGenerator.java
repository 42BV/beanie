/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package nl.mad.beanie.generator;


/**
 * Returns the values in sequence.
 *
 * @author Jeroen van Schagen
 * @since Apr 11, 2014
 */
public class SequentialValueGenerator implements ValueGenerator {
    
    private final Object[] values;
    
    private boolean repeatable = false;

    private int index = 0;
    
    public SequentialValueGenerator(Object[] values) {
        this.values = values;
    }

    public SequentialValueGenerator repeatable() {
        repeatable = true;
        return this;
    }

    @Override
    public Object generate(Class<?> valueType) {
        if (index >= values.length) {
            if (repeatable) {
                reset();
            } else {
                return null;
            }
        }
        return values[index++];
    }

    public void reset() {
        index = 0;
    }

}
