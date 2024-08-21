package com.example.canvasapi;

import com.example.webcanvasserver.TokenGenerator;
import com.example.webcanvasserver.TokenValidator;

import java.util.logging.Logger;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import jakarta.json.JsonObject;
import jakarta.json.Json;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Path("/auth")
public class AuthService {

    @POST
    @Path("/login")
    @Consumes("application/json")
    @Produces("application/json")
    public Response login(String jsonBody) {
        // Parse the JSON request body
        JsonObject jsonObject = Json.createReader(new StringReader(jsonBody)).readObject();
        String username = jsonObject.getString("username");
        String password = jsonObject.getString("password");

        boolean isAuthenticated = authenticateUser(username, password);

        if (isAuthenticated) {
            // Generate a token for the authenticated user
            String token = TokenGenerator.generateToken(username);
            return Response.ok().entity("{\"token\": \"" + token + "\"}").build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    private boolean authenticateUser(String username, String password) {
        String url = "jdbc:mysql://localhost:3306/canvas?useSSL=false";
        String dbUser = "root";
        String dbPassword = "root";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(url, dbUser, dbPassword);

            String query = "SELECT password FROM users WHERE username = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String storedPassword = resultSet.getString("password");
                return password.equals(storedPassword); // Direct comparison
            }

            System.out.print("User Authentication Called.");
            System.out.println("authenticateUser");

            resultSet.close();
            statement.close();
            connection.close();

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        return false;
    }


    @GET
    @Path("/validateToken")
    @Produces("application/json")
    public Response validateToken(@QueryParam("token") String token) {
        boolean isValid = TokenValidator.validateToken(token) != null;
        if (isValid) {
            return Response.ok().entity("{\"valid\": true}").build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"valid\": false}").build();
        }
    }
}


















//package com.example.canvasapi;
//
//import com.example.webcanvasserver.TokenGenerator;
//
//import jakarta.ws.rs.*;
//import jakarta.ws.rs.core.Response;
//
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.SQLException;
//import java.sql.Statement;
//
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//
//
//import jakarta.ws.rs.*;
//import jakarta.ws.rs.core.*;
//
//@Path("/auth")
//public class AuthService {
//
//    @POST
//    @Path("/login")
//    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response login(@FormParam("username") String username, @FormParam("password") String password) {
//        boolean isAuthenticated = authenticateUser(username, password);
//
//        if (isAuthenticated) {
//            // Generate a token for the authenticated user
//            String token = TokenGenerator.generateToken(username);
//            return Response.ok().entity("{\"token\": \"" + token + "\"}").build();
//        } else {
//            return Response.status(Response.Status.UNAUTHORIZED).build();
//        }
//    }
//
//    private boolean authenticateUser(String username, String password) {
//        String url = "jdbc:mysql://localhost:3306/canvas?useSSL=false";
//        String dbUser = "root";
//        String dbPassword = "root";
//
//        try {
//            Class.forName("com.mysql.cj.jdbc.Driver");
//            Connection connection = DriverManager.getConnection(url, dbUser, dbPassword);
//
//            String query = "SELECT password FROM users WHERE username = ?";
//            PreparedStatement statement = connection.prepareStatement(query);
//            statement.setString(1, username);
//
//            ResultSet resultSet = statement.executeQuery();
//
//            if (resultSet.next()) {
//                String storedPassword = resultSet.getString("password");
//                return password.equals(storedPassword); // Direct comparison
//            }
//
//            resultSet.close();
//            statement.close();
//            connection.close();
//
//        } catch (ClassNotFoundException | SQLException e) {
//            e.printStackTrace();
//        }
//
//        return false;
//    }
//}
