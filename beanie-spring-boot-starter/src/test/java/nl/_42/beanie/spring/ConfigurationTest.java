package nl._42.beanie.spring;

import nl._42.beanie.BeanBuilder;
import nl._42.beanie.spring.domain.Person;
import nl._42.beanie.spring.domain.PersonResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SpringBootTest
@RunWith(SpringRunner.class)
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

}
