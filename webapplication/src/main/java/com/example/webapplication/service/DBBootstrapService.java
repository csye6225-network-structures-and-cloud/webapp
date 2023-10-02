package com.example.webapplication.service;

import com.example.webapplication.model.Assignment;
import com.example.webapplication.model.User;
import com.example.webapplication.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class DBBootstrapService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService; // Inject the UserService

    @Autowired
    private AssignmentService assignmentService;

    @PostConstruct
    public void init() {
        try {
            // If the file is in resources/opt directory of your project
            String csvFilePath = new ClassPathResource("opt/users.csv").getFile().getAbsolutePath();

            userService.createUsersFromCSV(csvFilePath);

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
