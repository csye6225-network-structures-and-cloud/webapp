package com.example.webapplication.repository;

import com.example.webapplication.model.Assignment;
import com.example.webapplication.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssignmentRepository extends CrudRepository<Assignment, UUID> {

    List<Assignment> findAllByCreatedByUser(User user);

    Optional<Assignment> findByIdAndCreatedByUser(UUID id, User user);

}
