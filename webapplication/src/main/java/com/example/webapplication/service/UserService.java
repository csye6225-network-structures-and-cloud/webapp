package com.example.webapplication.service;

import com.example.webapplication.model.User;
import com.example.webapplication.repository.UserRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void createUsersFromCSV(String csvFilePath) throws IOException {
        try (Reader reader = new InputStreamReader(new FileInputStream(csvFilePath), StandardCharsets.UTF_8)) {

            Iterable<CSVRecord> records = CSVFormat.RFC4180.parse(reader);
            boolean isHeader = true;  // Assuming first row is header

            for (CSVRecord record : records) {
                if(isHeader) {
                    isHeader = false;
                    continue;
                }

                String first_name = record.get(0);
                String last_name = record.get(1);
                String email = record.get(2);
                String password = record.get(3);

                Optional<User> existingUser = userRepository.findUserByEmail(email);

                if (!existingUser.isPresent()) {
                    User user = new User();
                    user.setFirst_name(first_name);
                    user.setLast_name(last_name);
                    user.setPassword(passwordEncoder.encode(password));
                    user.setEmail(email);
                    user.setAccount_created(LocalDateTime.now());
                    user.setAccount_updated(LocalDateTime.now());
                    userRepository.save(user);
                }
            }
        } catch (IOException e) {
            throw new UserCsvImportException("Exception while importing users from CSV.", e);
        }



    // Custom exception class for user CSV import errors

    }

    // Custom exception class for user CSV import errors
    public static class UserCsvImportException extends RuntimeException {
        public UserCsvImportException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
