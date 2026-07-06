package com.andrei.demo.repository;

import com.andrei.demo.model.Task;
import com.andrei.demo.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    //taskuri dupa status
    List<Task> findByStatus(TaskStatus status);
    //taskuri overue
    List<Task> findByDeadlineBeforeAndStatusNot(LocalDateTime deadline, TaskStatus status);
    //taskurile unei persoane
    @Query("SELECT DISTINCT t FROM Task t JOIN t.assignedPersons p WHERE p.id = :personId")
    List<Task> findTasksByPersonId(@Param("personId") UUID personId);
    @Query("SELECT DISTINCT t FROM Task t LEFT JOIN FETCH t.assignedPersons")
    List<Task> findAllWithPersons();
}