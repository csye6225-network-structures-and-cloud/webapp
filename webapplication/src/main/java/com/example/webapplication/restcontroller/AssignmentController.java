package com.example.webapplication.restcontroller;

import com.example.webapplication.model.Assignment;
import com.example.webapplication.service.AssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
@ControllerAdvice
@RestController
@RequestMapping("/v1/assignments")
public class AssignmentController {

    @Autowired
    private AssignmentService assignmentService;

    @PostMapping
    public ResponseEntity<Assignment> createAssignment(
            @RequestBody Assignment assignment,
            @AuthenticationPrincipal UserDetails userDetails) {

        String userEmail = userDetails.getUsername();
        Assignment createdAssignment = assignmentService.createAssignment(userEmail, assignment);
        if (createdAssignment != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAssignment);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<List<Assignment>> getAllAssignments(@AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = userDetails.getUsername();
        List<Assignment> assignments = assignmentService.getAllAssignmentsForUser(userEmail);
        return ResponseEntity.ok(assignments);
    }


    // Get Specific Assignment by ID
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<Assignment> getAssignmentById(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = userDetails.getUsername();
        Assignment assignment = assignmentService.getAssignmentByIdAndUser(id, userEmail);

        if (assignment == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(assignment);
    }

    // Update Assignment by ID
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    public ResponseEntity<Assignment> updateAssignment(@PathVariable UUID id, @RequestBody Assignment updatedAssignment, @AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = userDetails.getUsername();
        Assignment assignment = assignmentService.updateAssignmentByIdAndUser(id, updatedAssignment, userEmail);

        if (assignment == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(assignment);
    }

    // Delete Assignment by ID
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = userDetails.getUsername();
        boolean isDeleted = assignmentService.deleteAssignmentByIdAndUser(id, userEmail);

        if (!isDeleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(AssignmentService.AssignmentValidationException.class)
    public ResponseEntity<String> handleAssignmentValidationException(AssignmentService.AssignmentValidationException ex) {
        return ResponseEntity.badRequest().body(null);
    }


    @ExceptionHandler(AssignmentService.UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException(AssignmentService.UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @ExceptionHandler(AssignmentService.ForbiddenException.class)
    public ResponseEntity<String> handleForbiddenException(AssignmentService.ForbiddenException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
    }

    @ExceptionHandler(AssignmentService.AssignmentNotFoundException.class)
    public ResponseEntity<String> handleAssignmentNotFoundException(AssignmentService.AssignmentNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

}
