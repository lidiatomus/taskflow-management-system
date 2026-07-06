package com.andrei.demo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Entity
@Data
@Table(name = "task")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false)
    private String description;

    @Column
    private LocalDateTime approvalRequestedAt;

    @Column
    private LocalDateTime approvalResolvedAt;

    @Column
    private String approvalComment;

    //task controleaza relatia deci punem @JoinTable
    //deoarece e many to many cream un table care
    //sa contina id-urile din ambele persons si tasks
    // => folosim @JoinTable
    @ManyToMany
    @JoinTable(
            name = "task_person",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "person_id")
    )
    private List<Person> assignedPersons;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    @Column(nullable = false)
    private LocalDateTime deadline;

    private LocalDateTime completedAt;

    @JsonProperty("timeRemaining")
    public String getTimeRemaining() {
        if (deadline == null || status == TaskStatus.DONE) {
            return null;
        }

        Duration duration = Duration.between(LocalDateTime.now(), deadline);

        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;

        if (duration.isNegative()) {
            return "Deadline passed";
        }

        return days + " days, " + hours + " hours, " + minutes + " minutes";
    }

    @JsonProperty("completionStatus")
    public String getCompletionStatus() {
        if (status != TaskStatus.DONE || completedAt == null || deadline == null) {
            return null;
        }

        if (!completedAt.isAfter(deadline)) {
            return "Completed on time";
        }

        return "Completed late";
    }
}
