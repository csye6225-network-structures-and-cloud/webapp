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
        //SnsClient snsClient = SnsClient.builder().region(Region.of("us-east-1")).credentialsProvider(ProfileCredentialsProvider.builder().profileName("demo").build()).build();
        try {
            // Check assignment details, like num_of_attempts and deadline
            Submission submission = new Submission();
            Assignment assignment = assignmentService.getAssignmentById(assignmentId);
            Assignment existingAssignment = assignmentRepository.findById(assignmentId).orElse(null);

            if (existingAssignment == null) {
                throw new AssignmentService.AssignmentNotFoundException("Assignment not found.");
            }

            User user = userRepository.findUserByEmail(userEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            if (submissionUrl == null || submissionUrl.trim().isEmpty()) {
                publishToSns(assignmentId, null, userEmail, snsClient, "FAILED");
                throw new SubmissionException("Submission URL is required.", HttpStatus.BAD_REQUEST);
            }
            if (!isValidUrl(submissionUrl)) {
                publishToSns(assignmentId, null, userEmail,snsClient, "FAILED");
                throw new SubmissionException("Invalid submission URL", HttpStatus.BAD_REQUEST);
            }
            List<Submission> previousSubmissions = submissionRepository.findAllByAssignmentIdAndUser(assignmentId, user);

            System.out.println(previousSubmissions.size());
            if (previousSubmissions.size() >= assignment.getNum_of_attempts()) {
                throw new AssignmentService.AssignmentValidationException("Exceeded the number of allowed attempts for this assignment");
            }
            submission.setAssignmentId(assignmentId);
            submission.setSubmissionUrl(submissionUrl);
            submission.setSubmissionDate(LocalDateTime.now(ZoneOffset.UTC));
            submission.setSubmissionUpdated(LocalDateTime.now(ZoneOffset.UTC));
            submission.setUser(user);
            if (submission.getSubmissionDate().isAfter(assignment.getDeadline())) {
                throw new AssignmentService.AssignmentValidationException("Submission deadline has passed.");
            }

            LOGGER.info("snsClient Initiated");
            publishToSns(assignmentId, submission, userEmail, snsClient, "SUCCESS");
            return submissionRepository.save(submission);

        } catch (AssignmentService.AssignmentNotFoundException ex) {
            LOGGER.error("Assignment Not Found");
            publishToSns(assignmentId, null, userEmail, snsClient, "FAILED");
            throw new SubmissionException("Assignment Not Found", HttpStatus.NOT_FOUND);
        } catch (AssignmentService.AssignmentValidationException ex) {
            LOGGER.error("Validation failed");
            publishToSns(assignmentId, null, userEmail,snsClient, "FAILED");
            throw new SubmissionException("Validation Failed: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
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

    private boolean isValidUrl(String urlString) {
        try {
            java.net.URL url = new URL(urlString);
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            huc.setRequestMethod("HEAD");
            huc.setConnectTimeout(100); // Set timeout as per your need
            huc.setReadTimeout(100); // Set timeout as per your need
            int responseCode = huc.getResponseCode();
            // Check if response code is HTTP OK (200)
            return (responseCode == HttpURLConnection.HTTP_OK);
        } catch (IOException e) {
            return false;
        }
    }

    private void publishToSns(UUID assignmentId, Submission submission,String userEmail, SnsClient snsClient, String status) {



        LOGGER.info("Inside sns");
        String message = null;
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> messageMap = new HashMap<>();
        messageMap.put("status", status);
        messageMap.put("userEmail", userEmail);

        if (submission != null) {
            messageMap.put("submissionUrl", submission.getSubmissionUrl());
        } else {
            messageMap.put("submissionUrl", "N/A");
        }
        messageMap.put("assignmentId", assignmentId.toString());

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




