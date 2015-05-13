/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package nl.mad.beanie.tester;

import java.io.IOException;

import nl.mad.beanie.util.Classes;

import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

public class HasNullaryConstructorFilter implements TypeFilter {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
        ClassMetadata metadata = metadataReader.getClassMetadata();
        Class<?> clazz = Classes.forName(metadata.getClassName());
        return Classes.hasNullaryConstructor(clazz);
    }

}