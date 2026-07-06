package com.andrei.demo.repository;

import com.andrei.demo.model.Departament;
import com.andrei.demo.model.DepartamentName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DepartamentRepository extends
JpaRepository<Departament, UUID> {
    Optional<Departament> findByName(DepartamentName name);

}
