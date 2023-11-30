package com.example.webapplication.service;

import com.example.webapplication.model.Assignment;
import com.example.webapplication.model.Submission;
import com.example.webapplication.model.User;
import com.example.webapplication.repository.AssignmentRepository;
import com.example.webapplication.repository.SubmissionRepository;
import com.example.webapplication.repository.UserRepository;
import com.example.webapplication.restcontroller.AssignmentController;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Service
public class SubmissionService {

    private final static Logger LOGGER = LoggerFactory.getLogger(SubmissionService.class);

    @Value("${sns.topicArn}")
    private String topicArn;

    @Value("${aws.region}")
    private String awsRegion;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private AssignmentRepository assignmentRepository;

    public Submission createSubmission(UUID assignmentId, String submissionUrl, String userEmail ) throws SubmissionException {

        SnsClient snsClient = SnsClient.builder().region(Region.of(awsRegion)).build();

        Submission submission = new Submission();

        List<Submission> previousSubmissions = null;
        int numOfPreviousSubmissions = 0;

        try {
            Assignment assignment = assignmentService.getAssignmentById(assignmentId);
            Assignment existingAssignment = assignmentRepository.findById(assignmentId).orElse(null);

            if (existingAssignment == null) {
                throw new AssignmentService.AssignmentValidationException("Assignment not found.");
            }
            User user = userRepository.findUserByEmail(userEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            previousSubmissions = submissionRepository.findAllByAssignmentIdAndUser(assignmentId, user);

            numOfPreviousSubmissions = (previousSubmissions != null) ? previousSubmissions.size() : 0;
            if (submissionUrl == null || submissionUrl.trim().isEmpty()) {
                String errMsg = "Submission File URL is required and should not be empty";
                throw new AssignmentService.AssignmentValidationException("Submission URL is required.");
            }
            if (!isValidUrl(submissionUrl)) {
                String errMsg = "Invalid submission URL: URL does not point to a ZIP file.";
                throw new AssignmentService.AssignmentValidationException("Invalid submission URL.");
            }
            System.out.println(previousSubmissions.size());
            if (previousSubmissions.size() >= assignment.getNum_of_attempts()) {
                throw new AssignmentService.ForbiddenException("Exceeded the number of allowed attempts for this assignment");
            }
            submission.setAssignmentId(assignmentId);
            submission.setSubmissionUrl(submissionUrl);
            submission.setSubmissionDate(LocalDateTime.now(ZoneOffset.UTC));
            submission.setSubmissionUpdated(LocalDateTime.now(ZoneOffset.UTC));
            submission.setUser(user);
            LOGGER.info("snsClient Initiated");
            String message = "Submission is Successful";

            if (submission.getSubmissionDate().isAfter(assignment.getDeadline())) {
                throw new AssignmentService.ForbiddenException("Submission deadline has passed.");
            }
            publishToSns(previousSubmissions.size(), assignmentId, submission, userEmail, snsClient, "SUCCESS", message);
            return submissionRepository.save(submission);

//        } catch (AssignmentService.AssignmentValidationException | AssignmentService.ForbiddenException ex) {
//            LOGGER.error("Validation failed");
//            String errmessage = "Submitted Assignment Validation is failed " + ex.getMessage();
//            publishToSns(previousSubmissions.size(), assignmentId, submission, userEmail, snsClient, "FAILED", errmessage);
//            throw new SubmissionException("Submission Failed: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
//        }
        }
            catch (Exception ex) {
                String status = "FAILED";
                HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

                if (ex instanceof AssignmentService.ForbiddenException) {
                    LOGGER.error(" Submission Forbidden " + ex.getMessage());
                    status = "FAILED";
                    httpStatus = HttpStatus.FORBIDDEN;
                } else if (ex instanceof AssignmentService.AssignmentValidationException) {
                    LOGGER.error("Validation failed: " + ex.getMessage());
                    status = "FAILED";
                    httpStatus = HttpStatus.BAD_REQUEST;
                }
                String errMessage = "Submission Failed: " + ex.getMessage();
                publishToSns(previousSubmissions.size(), assignmentId, submission, userEmail, snsClient, status, errMessage);
                throw new SubmissionException(errMessage, httpStatus);
            }
    }

    private boolean isValidUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            String contentType = url.openConnection().getContentType();
            int contentLength = url.openConnection().getContentLength();

            if (!"application/zip".equals(contentType) || contentLength == 0) {
                LOGGER.info("submission - bad request: submission URL does not return downloadable ZIP or ZIP is empty");
                throw new AssignmentService.AssignmentValidationException("Submission URL does not return downloadable ZIP or ZIP is empty");

            }
            return true;
        } catch (IOException e) {
            LOGGER.error("Error checking URL");
            return false;

        }
    }

    public class SubmissionException extends Exception {
        private HttpStatus status;

        public SubmissionException(String message, HttpStatus status) {
            super(message);
            this.status = status;
        }

        public HttpStatus getStatus() {
            return status;
        }
    }





    private void publishToSns(int attempts,UUID assignmentId, Submission submission,String userEmail, SnsClient snsClient, String status, String errorMessage) {



        LOGGER.info("Inside sns");
        String message = null;
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> messageMap = new HashMap<>();
        messageMap.put("attempts", String.valueOf(attempts));
        messageMap.put("status", status);
        messageMap.put("userEmail", userEmail);
        if (submission != null) {
            messageMap.put("submissionUrl", submission.getSubmissionUrl());
        } else {
            messageMap.put("submissionUrl", "Invalid URL");
        }
        messageMap.put("assignmentId", assignmentId.toString());
        messageMap.put("errorMessage", errorMessage);

        try {
            message = mapper.writeValueAsString(messageMap);
        } catch (JsonProcessingException e) {
            LOGGER.error("JSON processing error", e);
            // Handle the exception or rethrow as a RuntimeException
            throw new RuntimeException("Error processing JSON", e);
        }
        LOGGER.info("message"+ message);
        try {
            LOGGER.info("PublishRequest sns done");
            PublishRequest request = PublishRequest.builder()
                    .message(message)
                    .topicArn(topicArn)
                    .build();

            LOGGER.info("Topic Arn" + topicArn);
            PublishResponse result = snsClient.publish(request);
            System.out.println(result.messageId() + " Message sent. Status is " + result.sdkHttpResponse().statusCode());
            LOGGER.info(result.messageId() + " Message sent. Status is " + result.sdkHttpResponse().statusCode());
        } catch (SnsException e) {
            LOGGER.error("error"+ e.awsErrorDetails().errorMessage());
            System.err.println(e.awsErrorDetails().errorMessage());
        }
    }
}




