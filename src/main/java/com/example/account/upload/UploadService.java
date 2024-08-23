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
        // Get the token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No token provided.");
            return;
        }

        // Remove "Bearer " from the token
        String token = authHeader.substring(7);

        // Validate and extract the user_id from the token
        int userId = TokenManager.validateToken(token);
        if (userId == -1) {

            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            // Create a JSON response
            String jsonResponse = gson.toJson(Map.of("message", "Invalid Token."));
            response.getWriter().write(jsonResponse);

            return;
        }

        // If user does not have authority to upload
        if(!hasUploadPrivileges(userId)) {

            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            // Create a JSON response
            String jsonResponse = gson.toJson(Map.of("error", "You do Not Have Uploading Privileges."));
            response.getWriter().write(jsonResponse);

            return;
        }

        // Upload to site
        Part filePart = request.getPart("imageFile"); // "imageFile" was the name of the input in the javascript
        if(filePart == null || filePart.getSize() == 0) {

            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            // Create a JSON response
            String jsonResponse = gson.toJson(Map.of("error", "No File Provided."));
            response.getWriter().write(jsonResponse);

            return;
        }

        // Read the image data (byte[] holds raw binary data, without conversion, and is good at storing data like images, audio, packets, etc.)
        byte[] imageData = filePart.getInputStream().readAllBytes();

        boolean imageUploaded = uploadImage(imageData, userId);
        System.out.println(imageUploaded);

        if(imageUploaded) {
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK);
            // Create a JSON response
            String jsonResponse = gson.toJson(Map.of("success", "Image Uploaded Successfully."));
            response.getWriter().write(jsonResponse);

        } else {
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            // Create a JSON response
            String jsonResponse = gson.toJson(Map.of("error", "Image Failed to Upload."));
            response.getWriter().write(jsonResponse);
        }
    }

    private boolean uploadImage(byte[] imageData, int userId) {
        System.out.println("uploadImage");
        // Database credentials
        String url = "jdbc:mysql://localhost:3306/canvas?useSSL=false";
        String dbUser = "root";
        String dbPassword = "root";

        String query = "INSERT INTO user_images (image_data, user_id) VALUES (?, ?)";

        try {
            // Load the JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish the connection and perform the query
            try (Connection connection = DriverManager.getConnection(url, dbUser, dbPassword);
                 PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setBytes(1, imageData);
                statement.setInt(2, userId);


                int rowsAffected = statement.executeUpdate();
                return rowsAffected > 0;
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



    /*
    hasUploadPrivileges() returns true if the user trying to upload the image has the correct privilege level
     */
    private boolean hasUploadPrivileges(int userId) {

        // Database credentials
        String url = "jdbc:mysql://localhost:3306/canvas?useSSL=false";
        String dbUser = "root";
        String dbPassword = "root";

        String query = "SELECT privilege_level FROM users WHERE user_id = ?";

        try {
            // Load the JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish the connection and perform the query
            try (Connection connection = DriverManager.getConnection(url, dbUser, dbPassword);
                 PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, userId);


                try (ResultSet resultSet = statement.executeQuery()) {
                    // Ensure a result came back from the SQL
                    if(resultSet.next()) {
                        String privilege = resultSet.getString("privilege_level");
                        // Ensure user has either 'uploader' or 'admin' privilege
                        if("uploader".equals(privilege) || "admin".equals(privilege)) {
                            System.out.println("hasUploadPrivileges: True");
                            return true;
                        } else {
                            System.out.println("hasUploadPrivileges: False");
                            return false;
                        }
//                        return "uploader".equals(privilege) || "admin".equals(privilege);
                    }
                }
            } catch (SQLException e) {
                // Log SQL exceptions
                System.err.println("SQL error occurred while checking field: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            // Log exception if the driver class is not found
            System.err.println("JDBC Driver not found: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("hasUploadPrivileges: Finish without connecting");
        return false;
    }
}
