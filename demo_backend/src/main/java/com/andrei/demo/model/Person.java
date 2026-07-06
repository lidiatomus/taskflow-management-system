package com.andrei.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Data
@Table(name = "person")
public class Person {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    //ca sa nu mai se vada parola in response
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "password", nullable = false)
    private String password;

    private Integer age;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private PersonRole role;

    //person controleaza relatia deci aici punem @JoinColumn
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "departament_id")
    private Departament departament;

    //person nu controleaza relatia deci punem mappedBy si nu @JoinColumn
    @ToString.Exclude
    @ManyToMany(mappedBy = "assignedPersons")
    @JsonIgnore
    private List<Task> tasks;
}
