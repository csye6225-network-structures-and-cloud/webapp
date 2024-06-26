package com.example.webapplication.restcontroller;

import com.example.webapplication.model.Submission;
import com.example.webapplication.repository.SubmissionRepository;
import com.example.webapplication.service.SubmissionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.timgroup.statsd.StatsDClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;


@ControllerAdvice
@RestController
@RequestMapping("/v2/assignments")
public class AssignmentController {

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private SubmissionService submissionService;

    private final static Logger LOGGER = LoggerFactory.getLogger(AssignmentController.class);

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private StatsDClient metricsClient;

    @PostMapping
    public ResponseEntity<?> createAssignment(@RequestBody Assignment assignment, HttpServletRequest request, HttpServletResponse response) throws Exception {
        metricsClient.incrementCounter("endpoint./v1/.assignments.http.post");
        try {

            if (assignment.getName() != null && assignment.getName().matches("\\d+")) {
                LOGGER.error("Name should not be null and integer");
                throw new IllegalArgumentException("Name cannot be a number");
            }

            String loggedUser = authenticatedUser(request);

        // Check if user is not authorized
        if (loggedUser == null || loggedUser.split(" ").length != 2) {
            LOGGER.warn("Unauthorized attempt to create assignment by user: {}", loggedUser);
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
            LOGGER.info("Assignment created successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAssignment);
        } catch (IllegalArgumentException ex) {
            // Specific catch for the IllegalArgumentException for better error messaging
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        catch (Exception ex) {
            // This is a generic catch-all. You can refine this further based on the exceptions thrown by your service layer.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<List<Assignment>> getAllAssignments(@AuthenticationPrincipal UserDetails userDetails, @RequestBody(required = false) String body) {
        metricsClient.incrementCounter("endpoint./v1/.assignments.http.get");
        if (body != null && !body.isEmpty()) {
            LOGGER.error("GET request contains a body, which is unexpected.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("Cache-Control", "no-cache, no-store, must-revalidate")
                    .header("Pragma", "no-cache")
                    .header("X-Content-Type-Options", "nosniff")
                    .build();
        }


        try {
            List<Assignment> assignments = assignmentService.getAllAssignments();
            LOGGER.info("Assignments retrieved successfully");
            return ResponseEntity.ok(assignments);
        } catch (AssignmentService.AssignmentValidationException | AssignmentService.UserNotFoundException ex) {
            LOGGER.error("Assignments validation Exception");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (AssignmentService.ForbiddenException ex) {
            LOGGER.error("User Forbidden");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (AssignmentService.AssignmentNotFoundException ex) {
            LOGGER.error("Assignment Not Found ");
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<Assignment> getAssignmentById(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails, @RequestBody(required = false) String body) {
        metricsClient.incrementCounter("endpoint./v1/.assignments/.id.http.get");
        if (body != null && !body.isEmpty()) {
            LOGGER.error("Body should be empty For a get request");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("Cache-Control", "no-cache, no-store, must-revalidate")
                    .header("Pragma", "no-cache")
                    .header("X-Content-Type-Options", "nosniff")
                    .build();
        }

        try {
            Assignment assignment = assignmentService.getAssignmentById(id);
            LOGGER.info("Assignment retrieved successfully");
            return ResponseEntity.ok(assignment);
        } catch (AssignmentService.AssignmentValidationException | AssignmentService.UserNotFoundException ex) {
            LOGGER.error("Assignments validation Exception");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (AssignmentService.ForbiddenException ex) {
            LOGGER.error("User Forbidden");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (AssignmentService.AssignmentNotFoundException ex) {
            LOGGER.error("Assignment Not Found ");
            return ResponseEntity.notFound().build();
        }
    }


    // Update Assignment by ID
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateAssignment(@PathVariable UUID id, @RequestBody Assignment updatedAssignment, @AuthenticationPrincipal UserDetails userDetails) {

        metricsClient.incrementCounter("endpoint./v1/.assignments/.id.http.put");

        try {
            if (updatedAssignment.getName() != null && updatedAssignment.getName().matches("\\d+")) {
                LOGGER.error("Name should not be null and integer");
                throw new IllegalArgumentException("Name cannot be a number");
            }
            String userEmail = userDetails.getUsername();
            Assignment assignment = assignmentService.updateAssignmentByIdAndUser(id, updatedAssignment, userEmail);
            LOGGER.info("Assignment updated successfully");
            return ResponseEntity.noContent().build();

        } catch (AssignmentService.ForbiddenException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (AssignmentService.AssignmentNotFoundException ex) {
            LOGGER.error("Assignment Not Found ");
            return ResponseEntity.notFound().build();
        }
        catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
         catch (Exception ex) {
            // Generic error handler; refine this based on exceptions you expect from your service layer
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    // Delete Assignment by ID
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails, @RequestBody(required = false) String body) {

        metricsClient.incrementCounter("endpoint./v1/.assignments/.id.http.delete");

        if (submissionRepository.existsByAssignmentId(id)) {
            throw new AssignmentService.AssignmentValidationException("Cannot delete assignment as there are submissions against it.");
        }
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
                LOGGER.error("Assignment Not Found ");
                return ResponseEntity.notFound().build();
            }
            LOGGER.info("Assignment deleted successfully");
            return ResponseEntity.noContent().build();
        } catch (AssignmentService.UserNotFoundException ex) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        } catch (AssignmentService.ForbiddenException ex) {
            LOGGER.error("User Forbidden");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (AssignmentService.AssignmentNotFoundException ex) {
            LOGGER.error("Assignment Not Found ");
            return ResponseEntity.notFound().build();
        } catch (Exception ex) {
            LOGGER.error("Bad Request");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/submission")
    public ResponseEntity<?> submitAssignment(@PathVariable UUID id, @RequestBody String submissionData, @AuthenticationPrincipal UserDetails userDetails) {

        metricsClient.incrementCounter("endpoint./v1/.assignments/.id.submission.http.post");
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(submissionData);
            JsonNode submissionUrlNode = rootNode.get("submission_url");
            String submissionUrl = submissionUrlNode != null ? submissionUrlNode.asText() : null;
            Submission submission = submissionService.createSubmission(id, submissionUrl, userDetails.getUsername());
            LOGGER.info("Submission Successful");
            return ResponseEntity.status(HttpStatus.CREATED).body(submission);
        } catch (IOException ex) {
            LOGGER.error("Invalid submission data ");
            return ResponseEntity.badRequest().body("Invalid submission data.");
        } catch (SubmissionService.SubmissionException ex) {
            LOGGER.error("Error: " + ex.getMessage());
            return ResponseEntity.status(ex.getStatus()).body(ex.getMessage());
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
