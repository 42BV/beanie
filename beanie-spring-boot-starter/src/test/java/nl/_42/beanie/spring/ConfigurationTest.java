package nl._42.beanie.spring;

import nl._42.beanie.BeanBuilder;
import nl._42.beanie.spring.domain.Person;
import nl._42.beanie.spring.domain.PersonResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
public class ConfigurationTest {

    @Autowired
    private BeanBuilder beanie;

    @Test
    public void loads() {
        assertNotNull(beanie);
    }

    @Test
    public void generate() {
        Object generated = beanie.generate(String.class);
        assertEquals("value", generated);
    }

    @Test
    public void convert() {
        PersonResult result =
          beanie.start(Person.class)
                .withValue("name", "Jan")
                .map(PersonResult.class)
                .construct();

        assertEquals("Jan", result.name);
    }

    @Test
    public void save() {
        Person person =
            beanie.start(Person.class)
                .withValue("name", "Jan")
                .save();

        assertEquals("Jan", person.getName());
        assertNotNull(person.getId());
    }

    @Test
    public void save_withCustomSave() {
        Function<Person, Person> withName = (it) -> {
            it.setName(String.format("%s 2", it.getName()));
            return it;
        };

        Person person =
            beanie.start(Person.class)
                .withValue("name", "Jan")
                .setBeanSaver(withName)
                .save();

        assertEquals("Jan 2", person.getName());
        assertNull(person.getId());
    }

}
