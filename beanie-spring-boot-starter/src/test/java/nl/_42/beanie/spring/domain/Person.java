package nl._42.beanie.spring.domain;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import static jakarta.persistence.GenerationType.IDENTITY;

@Getter
@Setter
@Entity
public class Person {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  private String name;

}
