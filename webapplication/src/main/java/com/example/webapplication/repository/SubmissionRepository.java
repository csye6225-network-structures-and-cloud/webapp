package com.example.webapplication.repository;

import com.example.webapplication.model.Assignment;
import com.example.webapplication.model.Submission;
import com.example.webapplication.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface SubmissionRepository extends CrudRepository<Submission, UUID> {

    List<Submission> findAllByAssignmentIdAndUser(UUID assignmentId, User user);

    boolean existsByAssignmentId(UUID assignmentId);

}
