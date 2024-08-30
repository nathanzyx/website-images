package com.example.account.upload;

import com.example.account.login.TokenManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import com.google.gson.Gson;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

@WebServlet("/upload")
@MultipartConfig
public class UploadService extends HttpServlet {

    private static final Gson gson = new Gson(); // Create a Gson instance

    public enum UploadStatus {
        SUCCESS,
        IMAGE_ALREADY_EXISTS,
        SERVER_ERROR,
        INVALID_IMAGE_TYPE,
        NO_FILE_PROVIDED,
        INVALID_INFO_PROVIDED
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Get Coaster Name
        String name = (request.getParameter("name")).toLowerCase();
        Map<String, UploadStatus> uploadResults = new HashMap<>();

        if ((name.replaceAll("\\s", "")).isEmpty()) {
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            // Create a JSON response
            String jsonResponse = gson.toJson(Map.of("error", "Coaster name incorrect."));
            response.getWriter().write(jsonResponse);
        }

        // Get Elements
        String elementsJson = (request.getParameter("elements")).toLowerCase();
        String[] elements = (gson.fromJson(elementsJson, String[].class));

        // Prepare to handle multiple image files
        Collection<Part> fileParts = request.getParts().stream()
                .filter(part -> "images[]".equals(part.getName()))
                .collect(Collectors.toList());

        for (Part filePart : fileParts) {
            UploadStatus status = null;
            if (filePart == null || filePart.getSize() == 0) {
                status = UploadStatus.INVALID_INFO_PROVIDED;
                uploadResults.put(name, status);
                continue;
            }

            // Get image name
            String imageName = filePart.getSubmittedFileName();
            System.out.println("Uploading file: " + imageName);

            // Get image data
            InputStream fileContent = filePart.getInputStream();
            byte[] imageBytes = fileContent.readAllBytes();
            String imageType = filePart.getContentType();

            if (!isValidImageType(imageType)) {
                status = UploadStatus.INVALID_IMAGE_TYPE;
                uploadResults.put(imageName, status);
                continue;
            }

            try {
                status = uploadImage(name, elements, imageBytes, imageType, imageName);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }

            uploadResults.put(imageName, status);
        }

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        // Create a JSON response
        String jsonResponse = gson.toJson(uploadResults);
//        String jsonResponse = gson.toJson(Map.of("success", "Images Uploaded Successfully."));
        response.getWriter().write(jsonResponse);
    }

    private boolean isValidImageType(String imageType) {
        List<String> allowedImageTypes = Arrays.asList("image/jpeg", "image/png");
        return allowedImageTypes.contains(imageType);
    }

    private String calculateImageHash(byte[] imageData) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(imageData);
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private UploadStatus uploadImage(String name, String[] elements, byte[] imageData, String imageType, String imageName) throws NoSuchAlgorithmException {
        System.out.println("uploadImage");

        // Database credentials
        String url = "jdbc:mysql://localhost:3306/canvas?useSSL=false";
        String dbUser = "root";
        String dbPassword = "root";

        // SQL queries
        String checkImageHashQuery = "SELECT image_id FROM images WHERE image_hash = ?";
        String insertImageQuery = "INSERT INTO images (image_data, image_type, image_name, image_hash) VALUES (?, ?, ?, ?)";
        String insertCoasterQuery = "INSERT INTO coasters (coaster_name) VALUES (?)";
        String getCoasterIdQuery = "SELECT coaster_id FROM coasters WHERE coaster_name = ?";
        String insertElementQuery = "INSERT INTO elements (element_name) VALUES (?)";
        String getElementIdQuery = "SELECT element_id FROM elements WHERE element_name = ?";
        String insertImageCoasterQuery = "INSERT INTO image_coasters (image_id, coaster_id) VALUES (?, ?)";
        String insertImageElementQuery = "INSERT INTO image_elements (image_id, element_id) VALUES (?, ?)";

        String imageHash = calculateImageHash(imageData);

        try {
            // Load the JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish the connection
            try (Connection connection = DriverManager.getConnection(url, dbUser, dbPassword)) {
                // Ensure the image being uploaded does not already exist in the database
                try (PreparedStatement checkImageHashStmt = connection.prepareStatement(checkImageHashQuery)) {
                    checkImageHashStmt.setString(1, imageHash);
                    try (ResultSet rs = checkImageHashStmt.executeQuery()) {
                        if (rs.next()) {
                            // Image with the same hash already exists
                            return UploadStatus.IMAGE_ALREADY_EXISTS;
                        }
                    }
                }

                // Insert image
                int imageId;
                try (PreparedStatement insertImageStmt = connection.prepareStatement(insertImageQuery, Statement.RETURN_GENERATED_KEYS)) {
                    insertImageStmt.setBytes(1, imageData);
                    insertImageStmt.setString(2, imageType);
                    insertImageStmt.setString(3, imageName);
                    insertImageStmt.setString(4, imageHash);
                    int rowsAffected = insertImageStmt.executeUpdate();

                    if (rowsAffected > 0) {
                        try (ResultSet generatedKeys = insertImageStmt.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                imageId = generatedKeys.getInt(1);
                            } else {
                                throw new SQLException("Failed to get image ID.");
                            }
                        }
                    } else {
                        throw new SQLException("Failed to insert image.");
                    }
                }

                // Insert or get coaster_id
                int coasterId;
                try (PreparedStatement getCoasterIdStmt = connection.prepareStatement(getCoasterIdQuery)) {
                    getCoasterIdStmt.setString(1, name);
                    try (ResultSet rs = getCoasterIdStmt.executeQuery()) {
                        if (rs.next()) {
                            coasterId = rs.getInt("coaster_id");
                        } else {
                            try (PreparedStatement insertCoasterStmt = connection.prepareStatement(insertCoasterQuery, Statement.RETURN_GENERATED_KEYS)) {
                                insertCoasterStmt.setString(1, name);
                                int rowsAffected = insertCoasterStmt.executeUpdate();

                                if (rowsAffected > 0) {
                                    try (ResultSet generatedKeys = insertCoasterStmt.getGeneratedKeys()) {
                                        if (generatedKeys.next()) {
                                            coasterId = generatedKeys.getInt(1);
                                        } else {
                                            throw new SQLException("Failed to get coaster ID.");
                                        }
                                    }
                                } else {
                                    throw new SQLException("Failed to insert coaster.");
                                }
                            }
                        }
                    }
                }

                // Insert elements or get element_id
                for (String element : elements) {
                    int elementId;
                    try (PreparedStatement getElementIdStmt = connection.prepareStatement(getElementIdQuery)) {
                        getElementIdStmt.setString(1, element);
                        try (ResultSet rs = getElementIdStmt.executeQuery()) {
                            if (rs.next()) {
                                elementId = rs.getInt("element_id");
                            } else {
                                try (PreparedStatement insertElementStmt = connection.prepareStatement(insertElementQuery, Statement.RETURN_GENERATED_KEYS)) {
                                    insertElementStmt.setString(1, element);
                                    int rowsAffected = insertElementStmt.executeUpdate();

                                    if (rowsAffected > 0) {
                                        try (ResultSet generatedKeys = insertElementStmt.getGeneratedKeys()) {
                                            if (generatedKeys.next()) {
                                                elementId = generatedKeys.getInt(1);
                                            } else {
                                                throw new SQLException("Failed to get element ID.");
                                            }
                                        }
                                    } else {
                                        throw new SQLException("Failed to insert element.");
                                    }
                                }
                            }
                        }
                    }

                    // Insert into image_elements
                    try (PreparedStatement insertImageElementStmt = connection.prepareStatement(insertImageElementQuery)) {
                        insertImageElementStmt.setInt(1, imageId);
                        insertImageElementStmt.setInt(2, elementId);
                        insertImageElementStmt.executeUpdate();
                    }
                }

                // Insert into image_coasters
                try (PreparedStatement insertImageCoasterStmt = connection.prepareStatement(insertImageCoasterQuery)) {
                    insertImageCoasterStmt.setInt(1, imageId);
                    insertImageCoasterStmt.setInt(2, coasterId);
                    insertImageCoasterStmt.executeUpdate();
                }

                return UploadStatus.SUCCESS;
            } catch (SQLException e) {
                // Log SQL exceptions
                System.err.println("SQL error occurred while inserting image: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            // Log exception if the driver class is not found
            System.err.println("JDBC Driver not found: " + e.getMessage());
            e.printStackTrace();
        }
        return UploadStatus.SERVER_ERROR;
    }
}