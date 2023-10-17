package com.example.webapplication.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "Assignmentdata")
public class Assignment {

    @Id
    @GeneratedValue()
    @Column(name = "id")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @NotNull
    @NotBlank
    @JsonFormat(pattern = "^[^0-9]*$", shape = JsonFormat.Shape.STRING)
    @Column(name = "name")
    private String name;
    @NotNull

    @Min(1)
    @Max(10)
    @Column(name="points")
    private Integer points;
    @NotNull
    @Min(1)
    @Max(3)
    @Column(name = "num_of_attempts")
    private Integer num_of_attempts;
    @NotNull
    @Column(name = "deadline")
    private LocalDateTime deadline;

    @Column(name = "assignment_created")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime assignment_created;
    @Column(name = "assignment_updated")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime assignment_updated;

    @ManyToOne
    @JoinColumn(name = "createdByUser_id")
    private User createdByUser;

    @JsonIgnore
    public User getCreatedByUser() {
        return createdByUser;
    }

    public void setCreatedByUser(User createdByUser) {
        this.createdByUser = createdByUser;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public Integer getNum_of_attempts() {
        return num_of_attempts;
    }

    public void setNum_of_attempts(Integer num_of_attempts) {
        this.num_of_attempts = num_of_attempts;
    }

    public LocalDateTime getDeadline() {
        return this.deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public LocalDateTime getAssignment_created() {
        return assignment_created;
    }

    public void setAssignment_created(LocalDateTime account_created) {
        this.assignment_created = account_created;
    }

    public LocalDateTime getAssignment_updated() {
        return assignment_updated;
    }

    public void setAssignment_updated(LocalDateTime account_updated) {
        this.assignment_updated = account_updated;
    }

    @Override
    public String toString() {
        return "Assignment{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", points=" + points +
                ", num_of_attempts=" + num_of_attempts +
                ", deadline=" + deadline +
                ", assignment_created=" + assignment_created +
                ", assignment_updated=" + assignment_updated +
                '}';
    }
}
