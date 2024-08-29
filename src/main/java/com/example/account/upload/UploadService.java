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
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.stream.Collectors;

/*
ALTER TABLE users
ADD COLUMN privilege_level ENUM('user', 'uploader', 'admin') DEFAULT 'user';
 */


/*
CREATE TABLE user_images (
    image_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    image_data MEDIUMBLOB,
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);
 */

@WebServlet("/upload")
@MultipartConfig
public class UploadService extends HttpServlet {

    private static final Gson gson = new Gson(); // Create a Gson instance

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Get Coaster Name
        String name = (request.getParameter("name")).toLowerCase();

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
            if (filePart == null || filePart.getSize() == 0) {
                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                // Create a JSON response
                String jsonResponse = gson.toJson(Map.of("error", "No File Provided."));
                response.getWriter().write(jsonResponse);
                return;
            }

            // Get image name
            String imageName = filePart.getSubmittedFileName();
            System.out.println("Uploading file: " + imageName);

            // Get image data
            InputStream fileContent = filePart.getInputStream();
            byte[] imageBytes = fileContent.readAllBytes();
            String imageType = filePart.getContentType();

            if (!isValidImageType(imageType)) {
                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                // Create a JSON response
                String jsonResponse = gson.toJson(Map.of("error", "Invalid Image Type."));
                response.getWriter().write(jsonResponse);
                return;
            }

            boolean imageUploaded = uploadImage(name, elements, imageBytes, imageType, imageName);

            if (!imageUploaded) {
                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                // Create a JSON response
                String jsonResponse = gson.toJson(Map.of("error", "Image Failed to Upload."));
                response.getWriter().write(jsonResponse);
                return;
            }
        }

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        // Create a JSON response
        String jsonResponse = gson.toJson(Map.of("success", "Images Uploaded Successfully."));
        response.getWriter().write(jsonResponse);










//        // Get image data
//        Part filePart = request.getPart("image");
//        InputStream fileContent = filePart.getInputStream();
//        byte[] imageBytes = fileContent.readAllBytes();
//
//        if (filePart == null || filePart.getSize() == 0) {
//
//            response.setContentType("application/json");
//            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//            // Create a JSON response
//            String jsonResponse = gson.toJson(Map.of("error", "No File Provided."));
//            response.getWriter().write(jsonResponse);
//
//            return;
//        }
//
//        // Get image type
//        String imageType = filePart.getContentType();
//
//        if (!isValidImageType(imageType)) {
//
//            response.setContentType("application/json");
//            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//            // Create a JSON response
//            String jsonResponse = gson.toJson(Map.of("error", "Invalid Image Type."));
//            response.getWriter().write(jsonResponse);
//
//            return;
//        }
//
//        boolean imageUploaded = uploadImage(name, elements, imageBytes, imageType);
//
//        System.out.println(imageUploaded);
//
//        if (imageUploaded) {
//            response.setContentType("application/json");
//            response.setStatus(HttpServletResponse.SC_OK);
//            // Create a JSON response
//            String jsonResponse = gson.toJson(Map.of("success", "Image Uploaded Successfully."));
//            response.getWriter().write(jsonResponse);
//
//        } else {
//            response.setContentType("application/json");
//            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//            // Create a JSON response
//            String jsonResponse = gson.toJson(Map.of("error", "Image Failed to Upload."));
//            response.getWriter().write(jsonResponse);
//        }
    }

    private boolean isValidImageType(String imageType) {
        List<String> allowedImageTypes = Arrays.asList("image/jpeg", "image/png");
        return allowedImageTypes.contains(imageType);
    }

    private boolean uploadImage(String name, String[] elements, byte[] imageData, String imageType, String imageName) {
        System.out.println("uploadImage");

        // Database credentials
        String url = "jdbc:mysql://localhost:3306/canvas?useSSL=false";
        String dbUser = "root";
        String dbPassword = "root";

        // SQL queries
        String insertImageQuery = "INSERT INTO images (image_data, image_type, image_name) VALUES (?, ?, ?)";
        String insertCoasterQuery = "INSERT INTO coasters (coaster_name) VALUES (?)";
        String getCoasterIdQuery = "SELECT coaster_id FROM coasters WHERE coaster_name = ?";
        String insertElementQuery = "INSERT INTO elements (element_name) VALUES (?)";
        String getElementIdQuery = "SELECT element_id FROM elements WHERE element_name = ?";
        String insertImageCoasterQuery = "INSERT INTO image_coasters (image_id, coaster_id) VALUES (?, ?)";
        String insertImageElementQuery = "INSERT INTO image_elements (image_id, element_id) VALUES (?, ?)";

        try {
            // Load the JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish the connection
            try (Connection connection = DriverManager.getConnection(url, dbUser, dbPassword)) {
                // Insert image
                int imageId;
                try (PreparedStatement insertImageStmt = connection.prepareStatement(insertImageQuery, Statement.RETURN_GENERATED_KEYS)) {
                    insertImageStmt.setBytes(1, imageData);
                    insertImageStmt.setString(2, imageType);
                    insertImageStmt.setString(3, imageName);
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

                return true;
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
        return false;
    }

//    private boolean uploadImage(String name, String[] elements, byte[] imageData, String imageType) {
//        System.out.println("uploadImage");
//
//        // Database credentials
//        String url = "jdbc:mysql://localhost:3306/canvas?useSSL=false";
//        String dbUser = "root";
//        String dbPassword = "root";
//
//        // SQL queries
//        String insertImageQuery = "INSERT INTO images (image_data, image_type) VALUES (?, ?)";
//        String insertCoasterQuery = "INSERT INTO coasters (coaster_name) VALUES (?)";
//        String getCoasterIdQuery = "SELECT coaster_id FROM coasters WHERE coaster_name = ?";
//        String insertElementQuery = "INSERT INTO elements (element_name) VALUES (?)";
//        String getElementIdQuery = "SELECT element_id FROM elements WHERE element_name = ?";
//        String insertImageCoasterQuery = "INSERT INTO image_coasters (image_id, coaster_id) VALUES (?, ?)";
//        String insertImageElementQuery = "INSERT INTO image_elements (image_id, element_id) VALUES (?, ?)";
//
//        try {
//            // Load the JDBC driver
//            Class.forName("com.mysql.cj.jdbc.Driver");
//
//            // Establish the connection
//            try (Connection connection = DriverManager.getConnection(url, dbUser, dbPassword)) {
//                // Insert image
//                int imageId;
//                try (PreparedStatement insertImageStmt = connection.prepareStatement(insertImageQuery, Statement.RETURN_GENERATED_KEYS)) {
//                    insertImageStmt.setBytes(1, imageData);
//                    insertImageStmt.setString(2, imageType);
//                    int rowsAffected = insertImageStmt.executeUpdate();
//
//                    if (rowsAffected > 0) {
//                        try (ResultSet generatedKeys = insertImageStmt.getGeneratedKeys()) {
//                            if (generatedKeys.next()) {
//                                imageId = generatedKeys.getInt(1);
//                            } else {
//                                throw new SQLException("Failed to get image ID.");
//                            }
//                        }
//                    } else {
//                        throw new SQLException("Failed to insert image.");
//                    }
//                }
//
//                // Insert or get coaster_id
//                int coasterId;
//                try (PreparedStatement getCoasterIdStmt = connection.prepareStatement(getCoasterIdQuery)) {
//                    getCoasterIdStmt.setString(1, name);
//                    try (ResultSet rs = getCoasterIdStmt.executeQuery()) {
//                        if (rs.next()) {
//                            coasterId = rs.getInt("coaster_id");
//                        } else {
//                            try (PreparedStatement insertCoasterStmt = connection.prepareStatement(insertCoasterQuery, Statement.RETURN_GENERATED_KEYS)) {
//                                insertCoasterStmt.setString(1, name);
//                                int rowsAffected = insertCoasterStmt.executeUpdate();
//
//                                if (rowsAffected > 0) {
//                                    try (ResultSet generatedKeys = insertCoasterStmt.getGeneratedKeys()) {
//                                        if (generatedKeys.next()) {
//                                            coasterId = generatedKeys.getInt(1);
//                                        } else {
//                                            throw new SQLException("Failed to get coaster ID.");
//                                        }
//                                    }
//                                } else {
//                                    throw new SQLException("Failed to insert coaster.");
//                                }
//                            }
//                        }
//                    }
//                }
//
//                // Insert elements or get element_id
//                for (String element : elements) {
//                    int elementId;
//                    try (PreparedStatement getElementIdStmt = connection.prepareStatement(getElementIdQuery)) {
//                        getElementIdStmt.setString(1, element);
//                        try (ResultSet rs = getElementIdStmt.executeQuery()) {
//                            if (rs.next()) {
//                                elementId = rs.getInt("element_id");
//                            } else {
//                                try (PreparedStatement insertElementStmt = connection.prepareStatement(insertElementQuery, Statement.RETURN_GENERATED_KEYS)) {
//                                    insertElementStmt.setString(1, element);
//                                    int rowsAffected = insertElementStmt.executeUpdate();
//
//                                    if (rowsAffected > 0) {
//                                        try (ResultSet generatedKeys = insertElementStmt.getGeneratedKeys()) {
//                                            if (generatedKeys.next()) {
//                                                elementId = generatedKeys.getInt(1);
//                                            } else {
//                                                throw new SQLException("Failed to get element ID.");
//                                            }
//                                        }
//                                    } else {
//                                        throw new SQLException("Failed to insert element.");
//                                    }
//                                }
//                            }
//                        }
//                    }
//
//                    // Insert into image_elements
//                    try (PreparedStatement insertImageElementStmt = connection.prepareStatement(insertImageElementQuery)) {
//                        insertImageElementStmt.setInt(1, imageId);
//                        insertImageElementStmt.setInt(2, elementId);
//                        insertImageElementStmt.executeUpdate();
//                    }
//                }
//
//                // Insert into image_coasters
//                try (PreparedStatement insertImageCoasterStmt = connection.prepareStatement(insertImageCoasterQuery)) {
//                    insertImageCoasterStmt.setInt(1, imageId);
//                    insertImageCoasterStmt.setInt(2, coasterId);
//                    insertImageCoasterStmt.executeUpdate();
//                }
//
//                return true;
//            } catch (SQLException e) {
//                // Log SQL exceptions
//                System.err.println("SQL error occurred while inserting image: " + e.getMessage());
//                e.printStackTrace();
//            }
//        } catch (ClassNotFoundException e) {
//            // Log exception if the driver class is not found
//            System.err.println("JDBC Driver not found: " + e.getMessage());
//            e.printStackTrace();
//        }
//        return false;
//    }




//    private boolean uploadImage(String name, String[] elements, byte[] imageData, String imageType) {
//        System.out.println("uploadImage");
//        // Database credentials
//        String url = "jdbc:mysql://localhost:3306/canvas?useSSL=false";
//        String dbUser = "root";
//        String dbPassword = "root";
//
//        String query = "INSERT INTO user_images (image_data, user_id) VALUES (?, ?)";
//
//        try {
//            // Load the JDBC driver
//            Class.forName("com.mysql.cj.jdbc.Driver");
//
//            // Establish the connection and perform the query
//            try (Connection connection = DriverManager.getConnection(url, dbUser, dbPassword);
//                 PreparedStatement statement = connection.prepareStatement(query)) {
//                statement.setBytes(1, imageData);
////                statement.setInt(2, userId);
//
//
//                int rowsAffected = statement.executeUpdate();
//                return rowsAffected > 0;
//            } catch (SQLException e) {
//                // Log SQL exceptions
//                System.err.println("SQL error occurred while inserting image: " + e.getMessage());
//                e.printStackTrace();
//            }
//        } catch (ClassNotFoundException e) {
//            // Log exception if the driver class is not found
//            System.err.println("JDBC Driver not found: " + e.getMessage());
//            e.printStackTrace();
//        }
//        return false;
//    }
}






//    private boolean uploadImage(byte[] imageData, int userId) {
//        System.out.println("uploadImage");
//        // Database credentials
//        String url = "jdbc:mysql://localhost:3306/canvas?useSSL=false";
//        String dbUser = "root";
//        String dbPassword = "root";
//
//        String query = "INSERT INTO user_images (image_data, user_id) VALUES (?, ?)";
//
//        try {
//            // Load the JDBC driver
//            Class.forName("com.mysql.cj.jdbc.Driver");
//
//            // Establish the connection and perform the query
//            try (Connection connection = DriverManager.getConnection(url, dbUser, dbPassword);
//                PreparedStatement statement = connection.prepareStatement(query)) {
//                statement.setBytes(1, imageData);
//                statement.setInt(2, userId);
//
//
//                int rowsAffected = statement.executeUpdate();
//                return rowsAffected > 0;
//            } catch (SQLException e) {
//                // Log SQL exceptions
//                System.err.println("SQL error occurred while inserting image: " + e.getMessage());
//                e.printStackTrace();
//            }
//        } catch (ClassNotFoundException e) {
//            // Log exception if the driver class is not found
//            System.err.println("JDBC Driver not found: " + e.getMessage());
//            e.printStackTrace();
//        }
//        return false;
//    }



    /*
    hasUploadPrivileges() returns true if the user trying to upload the image has the correct privilege level
     */
//    private boolean hasUploadPrivileges(int userId) {
//
//        // Database credentials
//        String url = "jdbc:mysql://localhost:3306/canvas?useSSL=false";
//        String dbUser = "root";
//        String dbPassword = "root";
//
//        String query = "SELECT privilege_level FROM users WHERE user_id = ?";
//
//        try {
//            // Load the JDBC driver
//            Class.forName("com.mysql.cj.jdbc.Driver");
//
//            // Establish the connection and perform the query
//            try (Connection connection = DriverManager.getConnection(url, dbUser, dbPassword);
//                 PreparedStatement statement = connection.prepareStatement(query)) {
//                statement.setInt(1, userId);
//
//
//                try (ResultSet resultSet = statement.executeQuery()) {
//                    // Ensure a result came back from the SQL
//                    if(resultSet.next()) {
//                        String privilege = resultSet.getString("privilege_level");
//                        // Ensure user has either 'uploader' or 'admin' privilege
//                        if("uploader".equals(privilege) || "admin".equals(privilege)) {
//                            System.out.println("hasUploadPrivileges: True");
//                            return true;
//                        } else {
//                            System.out.println("hasUploadPrivileges: False");
//                            return false;
//                        }
////                        return "uploader".equals(privilege) || "admin".equals(privilege);
//                    }
//                }
//            } catch (SQLException e) {
//                // Log SQL exceptions
//                System.err.println("SQL error occurred while checking field: " + e.getMessage());
//                e.printStackTrace();
//            }
//        } catch (ClassNotFoundException e) {
//            // Log exception if the driver class is not found
//            System.err.println("JDBC Driver not found: " + e.getMessage());
//            e.printStackTrace();
//        }
//        System.out.println("hasUploadPrivileges: Finish without connecting");
//        return false;
//    }
//}


    /*
    isValidCoasterName returns true if the given coaster name exists in the database
     */
//    private boolean isValidCoasterName(String coasterName) {
//        // Database credentials
//        String url = "jdbc:mysql://localhost:3306/canvas?useSSL=false";
//        String dbUser = "root";
//        String dbPassword = "root";
//
//        // Validate against common SQL injection patterns
//        String[] disallowedPatterns = {"--", ";", "/*", "*/", "xp_", "exec", "select", "insert", "update", "delete", "drop"};
//        for (String pattern : disallowedPatterns) {
//            if (coasterName.toLowerCase().contains(pattern)) {
//                throw new IllegalArgumentException("Potential Dangerous Coaster Name Given By User: " + coasterName);
//            }
//        }
//
//        String query = "SELECT 1 FROM roller_coasters WHERE name = ?";
//
//        try {
//            // Load the JDBC driver
//            Class.forName("com.mysql.cj.jdbc.Driver");
//
//            // Establish the connection and perform the query
//            try (Connection connection = DriverManager.getConnection(url, dbUser, dbPassword);
//                 PreparedStatement statement = connection.prepareStatement(query)) {
//                statement.setString(1, coasterName);
//
//                try (ResultSet resultSet = statement.executeQuery()) {
//                    return resultSet.next();
//                }
//            } catch (SQLException e) {
//                // Log SQL exceptions
//                System.err.println("SQL error occurred while checking field: " + e.getMessage());
//                e.printStackTrace();
//                return false;
//            }
//        } catch (ClassNotFoundException e) {
//            // Log exception if the driver class is not found
//            System.err.println("JDBC Driver not found: " + e.getMessage());
//            e.printStackTrace();
//            return false;
//        }
//    }