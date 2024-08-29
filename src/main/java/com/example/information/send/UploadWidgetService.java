//package com.example.information.send;
//
//import com.example.account.login.TokenManager;
//import com.google.gson.Gson;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.annotation.MultipartConfig;
//import jakarta.servlet.annotation.WebServlet;
//import jakarta.servlet.http.HttpServlet;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import jakarta.servlet.http.Part;
//import jakarta.ws.rs.Consumes;
//import jakarta.ws.rs.Path;
//import jakarta.ws.rs.Produces;
//import jakarta.ws.rs.core.Response;
//
//import java.io.IOException;
//import java.sql.*;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Path("/widget")
//public class UploadWidgetService {
//
//    private static final Gson gson = new Gson(); // Create a Gson instance
//
//    @Path("/elements")
//    @Consumes("application/json")
//    @Produces("application/json")
//    public Response returnCoasterElements(String jsonBody) {
//        // Get the token
//        String authHeader = request.getHeader("Authorization");
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            return Response
//                    .status(Response.Status.BAD_REQUEST)
//                    .entity("{\"error\": \"Client Request is missing critical information.\"}")
//                    .type("application/json") // Ensure the response is treated as JSON
//                    .build();
//        }
//
//        // Remove "Bearer " from the token
//        String token = authHeader.substring(7);
//
//        // Validate and extract the user_id from the token
//        int userId = TokenManager.validateToken(token);
//        if (userId == -1) {
//            return Response
//                    .status(Response.Status.UNAUTHORIZED)
//                    .entity("{\"error\": \"Unauthorized access to this data.\"}")
//                    .type("application/json") // Ensure the response is treated as JSON
//                    .build();
//        }
//
//
//        // Fetch the coaster elements
//        Map<String, Boolean> coasterElements = getCoasterElements();
//        if (coasterElements == null) {
//            return Response
//                    .status(Response.Status.INTERNAL_SERVER_ERROR)
//                    .entity("{\"error\": \"Unable to retrieve coaster elements.\"}")
//                    .type("application/json")
//                    .build();
//        }
//
//        String jsonResponse = gson.toJson(coasterElements);
//        return Response
//                .status(Response.Status.OK)
//                .entity(jsonResponse)
//                .type("application/json")
//                .build();
//    }
//
//    private Map<String, Boolean> getCoasterElements() {
//        // Database credentials
//        String url = "jdbc:mysql://localhost:3306/canvas?useSSL=false";
//        String dbUser = "root";
//        String dbPassword = "root";
//
//        String query = "SELECT element_name, is_inversion FROM coaster_elements";
//
//        Map<String, Boolean> coasterElements = new HashMap<>();
//
//        try {
//            // Load the JDBC driver
//            Class.forName("com.mysql.cj.jdbc.Driver");
//
//            // Establish the connection and perform the query
//            try (Connection connection = DriverManager.getConnection(url, dbUser, dbPassword);
//                 PreparedStatement statement = connection.prepareStatement(query);
//                 ResultSet resultSet = statement.executeQuery()) {
//
//                while (resultSet.next()) {
//                    String elementName = resultSet.getString("element_name");
//                    boolean isInversion = resultSet.getBoolean("is_inversion");
//                    coasterElements.put(elementName, isInversion);
//                }
//
//            } catch (SQLException e) {
//                // Log SQL exceptions
//                System.err.println("SQL error occurred while fetching coaster elements: " + e.getMessage());
//                e.printStackTrace();
//                return null;
//            }
//        } catch (ClassNotFoundException e) {
//            // Log exception if the driver class is not found
//            System.err.println("JDBC Driver not found: " + e.getMessage());
//            e.printStackTrace();
//            return null;
//        }
//
//        return coasterElements;
//    }
//}
