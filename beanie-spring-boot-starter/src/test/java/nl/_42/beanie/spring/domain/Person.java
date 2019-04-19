package nl._42.beanie.spring.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import static javax.persistence.GenerationType.IDENTITY;

@Getter
@Setter
@Entity
public class Person {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  private String name;

}
