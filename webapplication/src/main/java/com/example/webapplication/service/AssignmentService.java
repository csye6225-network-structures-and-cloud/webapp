package com.example.webapplication.service;

import com.example.webapplication.model.Assignment;
import com.example.webapplication.model.User;
import com.example.webapplication.repository.AssignmentRepository;
import com.example.webapplication.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class AssignmentService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    public Assignment createAssignment(String email, Assignment assignment){


        // These conditions were incorrect; corrected to check ranges
        if (assignment.getPoints() < 1 || assignment.getPoints() > 10) {
            throw new AssignmentValidationException("Assignment points must be between 1 and 10.");
        }



        if (assignment.getNum_of_attempts() < 1 || assignment.getNum_of_attempts() > 3) {
            throw new AssignmentValidationException("Assignment attempts must be between 1 and 3.");
        }



        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // No need to re-set the assignment properties as they come in the request
        assignment.setAssignment_created(LocalDateTime.now());
        assignment.setAssignment_updated(LocalDateTime.now());
        assignment.setCreatedByUser(user);

        return assignmentRepository.save(assignment);
    }

    public List<Assignment> getAllAssignmentsForUser(String userEmail) {

        User user = userRepository.findUserByEmail(userEmail).orElseThrow(() -> new ForbiddenException("records are access to only existing users"));
        return assignmentRepository.findAllByCreatedByUser(user);
    }

    public Assignment getAssignmentByIdAndUser(UUID id, String userEmail) {
        User user = userRepository.findUserByEmail(userEmail).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Assignment assignment = assignmentRepository.findById(id).orElse(null);
        if (assignment != null &&  assignment.getCreatedByUser()==null && !assignment.getCreatedByUser().equals(user)) {
            throw new ForbiddenException("You are not authorized to access this assignment");
        }
        return assignment;
    }


    public List<Assignment> getAllAssignments() {
        Iterable<Assignment> assignments = assignmentRepository.findAll();
        return StreamSupport.stream(assignments.spliterator(), false)
                .collect(Collectors.toList());
    }

    public Assignment getAssignmentById(UUID id) {
        return assignmentRepository.findById(id).orElseThrow(() -> new AssignmentNotFoundException("Assignment not found."));
    }

    public Assignment updateAssignmentByIdAndUser(UUID id, Assignment updatedAssignment, String userEmail) {


        User user = userRepository.findUserByEmail(userEmail).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Assignment existingAssignment = assignmentRepository.findById(id).orElse(null);

        if (existingAssignment == null) {
            throw new AssignmentNotFoundException("Assignment not found.");
        }

        if (!existingAssignment.getCreatedByUser().equals(user)) {
            throw new ForbiddenException("You are not authorized to update this assignment");
        }

        existingAssignment.setName(updatedAssignment.getName());
        existingAssignment.setPoints(updatedAssignment.getPoints());
        existingAssignment.setNum_of_attempts(updatedAssignment.getNum_of_attempts());
        existingAssignment.setDeadline(updatedAssignment.getDeadline());
        existingAssignment.setAssignment_updated(LocalDateTime.now());

        return assignmentRepository.save(existingAssignment);
    }

    public boolean deleteAssignmentByIdAndUser(UUID id, String userEmail) {
        User user = userRepository.findUserByEmail(userEmail).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Assignment assignment = assignmentRepository.findById(id).orElse(null);

        if (assignment == null) {
            return false;
        }

        if (!assignment.getCreatedByUser().equals(user)) {
            throw new ForbiddenException("You are not authorized to delete this assignment");
        }

        assignmentRepository.delete(assignment);
        return true;
    }


    public static class AssignmentNotFoundException extends RuntimeException {
        public AssignmentNotFoundException(String message) {
            super(message);
        }
    }

    public static class ForbiddenException extends RuntimeException {
        public ForbiddenException(String message) {
            super(message);
        }
    }

    public static class AssignmentValidationException extends RuntimeException {
        public AssignmentValidationException(String message) {
            super(message);
        }
    }





    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }
}






