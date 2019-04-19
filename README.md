[![Build Status](https://travis-ci.org/42BV/beanie.svg?branch=master)](https://travis-ci.org/42BV/beanie)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/86775ea7cd154b4c89547f4b9533ea52)](https://www.codacy.com/app/42bv/beanie)
[![BCH compliance](https://bettercodehub.com/edge/badge/42BV/beanie?branch=master)](https://bettercodehub.com/)
[![Codecov](https://codecov.io/gh/42bv/beanie/branch/master/graph/badge.svg)](https://codecov.io/gh/42bv/beanie)
[![Maven central](https://maven-badges.herokuapp.com/maven-central/nl.42/beanie/badge.svg)](https://maven-badges.herokuapp.com/maven-central/nl.42/beanie)
[![Javadocs](https://www.javadoc.io/badge/nl.42/beanie.svg)](https://www.javadoc.io/doc/nl.42/beanie)
[![Apache 2](http://img.shields.io/badge/license-Apache%202-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

# Beanie

Testing is important to guarantee the functionality of your application. These tests often rely on complex data
structure as input. 

Beanie helps with the construction of these input beans, making tests as readable and understandable as possible, 
promoting reuse of input data over multiple tests. Beans are constructed using the `BeanBuilder` API:

```java
Person person = 
    builder.start(Person.class) // Construct person
           .withValue("email", "jan@42.nl") // Set email
           .withValue("hobbies", "coding") // Add hobbies
           .withValue("hobbies", "gaming") // Add hobbies
           .generateValue("name", new RandomStringGenerator(8, 12)) // Generate name
           .fill() // Generate other values (irrelevant for this test)
           .save(); // Construct and persist the person

```

Or define a custom builder interface for your data type. This allows for more type-safe data creation and even
allows us to define default 'convenience' methods:

```java
public interface PersonBuilder extends EditableBeanBuildCommand<Person> {
    
    PersonBuilder withEmail(String email);
    
    PersonBuilder withName();
    PersonBuilder withName(String name);
    PersonBuilder withName(ValueGenerator generator);
    
    PersonBuilder withHobbies(String hobby);
    PersonBuilder withHobbies(Set<String> hobbies);
    
    default PersonBuilder coder() {
        return this.withHobbies("coding");
    }
    
}
```

Custom builders are also started using the `BeanBuilder`:

```java
Person person = 
    builder.startAs(PersonBuilder.class)
           .withEmail("jan@42.nl")
           .withName("Jan")
           .coder()
           .save();
                             
```
    
## Specific builder

It is recommended to create a specific builder per type of bean. This type specific builder can have factory 
methods, such as `jan()`, which get used in multiple unit tests:

```java
@Component
@AllArgsConstructor
public class Persons {
    
    private final BeanBuilder builder;
    
    public Person jan() {
        return builder.startAs(PersonBuilder.class)
                      .withEmail("jan@42.nl")
                      .withName("Jan")
                      .coder()
                      .save();
    }
    
}                             
```

Usage of builders keeps our unit test simple and concise:

```java
public class PersonServiceTest {
    
    private PersonService personService;
    private Persons persons;
    
    @Test
    public void change_email() {
        Person jan = persons.jan();
        
        Person updated = personService.changeEmail(jan, "piet@42.nl");
        assertEquals("piet@42.nl", updated.getEmail());
    }
    
}
```

## Getters and setters

Test the getter and setter methods for all beans in a package, using this one liner:

```java
new BeanTester().includeAll().verifyBeans("some.package");
```

Bean tester will scan the package for beans and verify the getter/setter methods, using both `null` and `non-null`
generated values. This way we test the consistency of changing a property and retrieving it afterwards.

## License

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

