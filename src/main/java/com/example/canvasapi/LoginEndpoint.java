package com.example.canvasapi;


import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Path("/login")
public class LoginEndpoint {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/canvas?useSSL=false";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";

    public static boolean authenticateUser(String username, String password) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Connected to mySQL: " + DB_URL);

            // SQL query to select the user by username
            String query = "SELECT password FROM users WHERE username = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);

            ResultSet resultSet = statement.executeQuery();

            // Check if a user with the provided username exists
            if (resultSet.next()) {
                String storedPassword = resultSet.getString("password");

                // Compare the provided password with the stored hashed password
                // In a real application, you should hash the provided password and compare it to the stored hash
                return password.equals(storedPassword); // This is insecure; use hashing in production
            }

            return false;
        } catch (ClassNotFoundException e) {
            System.out.println("Driver class not found.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("SQL error.");
            e.printStackTrace();
        }
        return false;
    }




    @POST
    public Response login(@QueryParam("username") String username, @QueryParam("password") String password) {
        // Perform authentication logic (check credentials)
        boolean isAuthenticated = authenticateUser(username, password);

        if (isAuthenticated) {
            // Generate a token for the authenticated user
            String token = LoginService.handleLogin(username);
            return Response.ok().entity("{\"token\": \"" + token + "\"}").build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }
}
