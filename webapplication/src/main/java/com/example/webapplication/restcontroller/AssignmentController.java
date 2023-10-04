package com.example.webapplication.restcontroller;

import org.springframework.validation.ObjectError;
import com.example.webapplication.model.Assignment;
import com.example.webapplication.service.AssignmentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.security.access.AccessDeniedException;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;


@ControllerAdvice
@RestController
@RequestMapping("/v1/assignments")
public class AssignmentController {

    @Autowired
    private AssignmentService assignmentService;

    @PostMapping
    public ResponseEntity<?> createAssignment(@RequestBody Assignment assignment, HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
        String loggedUser = authenticatedUser(request);

        // Check if user is not authorized
        if (loggedUser == null || loggedUser.split(" ").length != 2) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write(""); // Ensure empty content
            response.getWriter().flush();
            response.getWriter().close();
            return null; // Return null after setting the response directly
        }

        String username = loggedUser.split(" ")[0];
        String password = loggedUser.split(" ")[1];

        String userEmail = username;
        System.out.println("userEmail : " + userEmail);
        Assignment createdAssignment = assignmentService.createAssignment(userEmail, assignment);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAssignment);
        } catch (Exception ex) {
            // This is a generic catch-all. You can refine this further based on the exceptions thrown by your service layer.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }


    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<List<Assignment>> getAllAssignments(@AuthenticationPrincipal UserDetails userDetails, @RequestBody(required = false) String body) {
        if (body != null && !body.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("Cache-Control", "no-cache, no-store, must-revalidate")
                    .header("Pragma", "no-cache")
                    .header("X-Content-Type-Options", "nosniff")
                    .build();
        }

        try {
            List<Assignment> assignments = assignmentService.getAllAssignments();
            return ResponseEntity.ok(assignments);
        } catch (AssignmentService.AssignmentValidationException | AssignmentService.UserNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (AssignmentService.ForbiddenException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (AssignmentService.AssignmentNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<Assignment> getAssignmentById(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails, @RequestBody(required = false) String body) {
        if (body != null && !body.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("Cache-Control", "no-cache, no-store, must-revalidate")
                    .header("Pragma", "no-cache")
                    .header("X-Content-Type-Options", "nosniff")
                    .build();
        }

        try {
            Assignment assignment = assignmentService.getAssignmentById(id);
            return ResponseEntity.ok(assignment);
        } catch (AssignmentService.AssignmentValidationException | AssignmentService.UserNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (AssignmentService.ForbiddenException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (AssignmentService.AssignmentNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    // Update Assignment by ID
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    public ResponseEntity<Assignment> updateAssignment(@PathVariable UUID id, @RequestBody Assignment updatedAssignment, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String userEmail = userDetails.getUsername();
            Assignment assignment = assignmentService.updateAssignmentByIdAndUser(id, updatedAssignment, userEmail);
            return ResponseEntity.ok(assignment);

        } catch (AssignmentService.ForbiddenException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (AssignmentService.AssignmentNotFoundException ex) {
            return ResponseEntity.notFound().build();
        } catch (Exception ex) {
            // Generic error handler; refine this based on exceptions you expect from your service layer
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    // Delete Assignment by ID
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails, @RequestBody(required = false) String body) {
        if (body != null && !body.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("Cache-Control", "no-cache, no-store, must-revalidate")
                    .header("Pragma", "no-cache")
                    .header("X-Content-Type-Options", "nosniff")
                    .build();
        }
        try {
            String userEmail = userDetails.getUsername();
            boolean isDeleted = assignmentService.deleteAssignmentByIdAndUser(id, userEmail);

            if (!isDeleted) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.noContent().build();
        } catch (AssignmentService.UserNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        } catch (AssignmentService.ForbiddenException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (AssignmentService.AssignmentNotFoundException ex) {
            return ResponseEntity.notFound().build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }


    //Custom Exception Handling

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


    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Void> handleNotFound() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Void> handleMethodArgumentTypeMismatch() {
        return ResponseEntity.notFound().build();
    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Void> handleHttpMessageNotReadable() {
        return ResponseEntity.badRequest().build();  // Returns a 400 Bad Request when no body is sent body
    }



    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Void> handleAccessDeniedException(AccessDeniedException ex, HttpServletResponse response) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        // You can extract more details from the exception if needed.
        String errorMessage = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(ObjectError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }


    private String authenticatedUser(HttpServletRequest request){

        String tokenEnc = request.getHeader("Authorization").split(" ")[1];
        byte[] token = Base64.getDecoder().decode(tokenEnc);
        String decodedStr = new String(token, StandardCharsets.UTF_8);

        String userName = decodedStr.split(":")[0];
        String passWord = decodedStr.split(":")[1];
        System.out.println("Value of Token" + " "+ decodedStr);

        return (userName + " " + passWord);

    }






}
