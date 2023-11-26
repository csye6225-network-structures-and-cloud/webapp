package com.example.webapplication.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "submissiondata")
public class Submission {


    @Id
    @GeneratedValue()
    @Column(name = "id")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name="assignment_Id")
    private UUID assignmentId;


    @NotBlank
    @Column(name ="submission_url")
    private String submissionUrl;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    @Column(name ="submission_date")
    private LocalDateTime submissionDate;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    @Column(name ="submission_updated")
    private LocalDateTime submissionUpdated;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name ="user_id")
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public UUID getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(UUID assignmentId) {
        this.assignmentId = assignmentId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }



    public String getSubmissionUrl() {
        return submissionUrl;
    }

    public void setSubmissionUrl(String submissionUrl) {
        this.submissionUrl = submissionUrl;
    }

    public LocalDateTime getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(LocalDateTime submissionDate) {
        this.submissionDate = submissionDate;
    }


    public LocalDateTime getSubmissionUpdated() {
        return submissionUpdated;
    }

    public void setSubmissionUpdated(LocalDateTime submissionUpdated) {
        this.submissionUpdated = submissionUpdated;
    }

    @Override
    public String toString() {
        return "Submission{" +
                "id=" + id +
                ", assignmentId=" + assignmentId +
                ", submissionUrl='" + submissionUrl + '\'' +
                ", submissionDate=" + submissionDate +
                ", submissionUpdated=" + submissionUpdated +
                '}';
    }
}
