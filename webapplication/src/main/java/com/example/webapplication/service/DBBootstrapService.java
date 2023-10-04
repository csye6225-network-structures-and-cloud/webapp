package com.example.webapplication.service;

import com.example.webapplication.model.Assignment;
import com.example.webapplication.model.User;
import com.example.webapplication.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class DBBootstrapService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService; // Inject the UserService

    @Autowired
    private AssignmentService assignmentService;

    @Value("${app.environment}")
    private String environment;
    @PostConstruct
    public void init() {
        try {

            String csvFilePath;

            if ("local".equalsIgnoreCase(environment)) {
                // If the file is in resources/opt directory of your project
                csvFilePath = new ClassPathResource("opt/users.csv").getFile().getAbsolutePath();
                userService.createUsersFromCSV(csvFilePath);
            } else { // Assuming it's production
                userService.createUsersFromCSV("/opt/users.csv");
            }


        } catch (IOException e) {

            throw new DatabaseInitializationException("Error bootstrapping the database.", e);
        }
    }

    // Custom exception class for database  errors
    public static class DatabaseInitializationException extends RuntimeException {
        public DatabaseInitializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
