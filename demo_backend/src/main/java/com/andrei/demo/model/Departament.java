package com.andrei.demo.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.List;
import java.util.UUID;

@Entity
@Data
@Table(name = "departament")
public class Departament {
    //departament nu controleaza relatia deci punem mappedBy si nu @JoinColumn
    @JsonIgnore
    @ToString.Exclude
    @OneToMany(mappedBy = "departament")
    private List<Person> members;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true)
    private DepartamentName name;

    @Column(name = "description", nullable = false)
    private String description;
}
