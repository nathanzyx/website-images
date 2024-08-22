package com.example.account.signup;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.io.StringReader;
import java.sql.*;
import java.time.Period;
import java.util.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Path("/signup")
public class SignupService {

    @POST
//    @Path("/login")
    @Consumes("application/json")
    @Produces("application/json")
    public Response signup(String jsonBody) {
        // Parse the JSON request body, Extract username and password sent to server by user
        JsonObject jsonObject = Json.createReader(new StringReader(jsonBody)).readObject();
        String username = jsonObject.getString("username");
        String password = jsonObject.getString("password");
        String email = jsonObject.getString("email");
        String firstname = jsonObject.getString("firstname");
        String lastname = jsonObject.getString("lastname");
        String birthdate = jsonObject.getString("birthdate");


//        Map<String, String> fieldsToCheck = new HashMap<>();
//        fieldsToCheck.put("username", username);
//        fieldsToCheck.put("email", email);
//
//        Map<String, Boolean> results = SQLFieldCheck(fieldsToCheck);


        Map<String, String> validationErrors = new HashMap<>();

        // Validate username
        if (!isValidUsernameOrPassword(username)) {
            validationErrors.put("username","Invalid Username! (max length: 25 characters)");
        } else if (SQLFieldTaken("username", username)) {
            validationErrors.put("username","Username is already in use!");
        }


        // Validate password
        if (!isValidUsernameOrPassword(password)) {
            validationErrors.put("password","Invalid Password! (max length: 25 characters)");
        }

        // Validate email
        if (!isValidEmail(email)) {
            validationErrors.put("email","Invalid Email!");
        } else if (SQLFieldTaken("email", email)) {
            validationErrors.put("email","Email is already in use!");
        }

        // Validate firstname
        if (!isValidName(firstname)) {
            validationErrors.put("firstname","Invalid First Name! (max length: 25 characters)");
        }

        // Validate lastname
        if (!isValidName(lastname)) {
            validationErrors.put("lastname","Invalid Last Name! (max length: 25 characters)");
        }

        // Validate birthdate
        if (!isValidBirthdate(birthdate)) {
            validationErrors.put("birthdate","Invalid Birth Date! (users must be over 13 years of age)");
        }


        // If errors arise while attempting to create an account
        if (!validationErrors.isEmpty()) {
            JsonObject responseJson = Json.createObjectBuilder(validationErrors).build();

            // Return a bad request response with the validation errors
            return Response.status(Response.Status.CONFLICT)
                    .entity(responseJson.toString())
                    .build();
        }

        // If no errors while attempting to create an account the account will be created
        boolean accountCreated = createAccount(username, password, email, firstname, lastname, birthdate);

        if (accountCreated) {
            return Response.status(Response.Status.CREATED)
                    .entity("{\"message\": \"Account successfully created!\"}")
                    .build();
        } else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Account creation failed.\"}")
                    .build();
        }
    }

    private boolean isValidUsernameOrPassword(String entry) {
        return (!entry.isEmpty() && entry.length() <= 25);
    }

    private boolean isValidEmail(String email) {
        return (email.length() <= 255 && email.length() >= 3 && email.contains("@"));
    }

    private boolean isValidName(String name) {
        return (!name.isEmpty() && name.length() <= 25);
    }

    public static boolean isValidBirthdate(String birthdateStr) {
        if (!isValidDate(birthdateStr)) {
            return false; // Invalid date format
        }

        // Parse the birthdate string to a LocalDate object
        LocalDate birthdate = LocalDate.parse(birthdateStr);

        // Get the current date
        LocalDate currentDate = LocalDate.now();

        // Calculate the period between the birthdate and the current date
        Period age = Period.between(birthdate, currentDate);

        // Check if the user is 13 years or older
        return age.getYears() >= 13;
    }

    public static boolean isValidDate(String dateStr) {
        if (dateStr == null) {
            return false;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try {
            LocalDate date = LocalDate.parse(dateStr, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }


    private Boolean SQLFieldTaken(String field, String entry) {
        // Database credentials
        String url = "jdbc:mysql://localhost:3306/canvas?useSSL=false";
        String dbUser = "root";
        String dbPassword = "root";

        // Define allowed fields
        List<String> allowedFields = Arrays.asList("username", "password", "email", "firstname", "lastname", "birthdate");

        // Validate the field
        if (!allowedFields.contains(field)) {
            throw new IllegalArgumentException("Invalid field provided: " + field);
        }

        String query = "SELECT 1 FROM users WHERE " + field + " = ?";

        try (Connection connection = DriverManager.getConnection(url, dbUser, dbPassword);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, entry);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

//        try {
//            Class.forName("com.mysql.cj.jdbc.Driver");
//            Connection connection = DriverManager.getConnection(url, dbUser, dbPassword);
//
//            // Query username and password
//            String query = "SELECT " + field + " FROM users WHERE " + field + " = ?";
//            PreparedStatement statement = connection.prepareStatement(query);
//            statement.setString(1, entry);
//
//            ResultSet resultSet = statement.executeQuery();
//
//            // If the entry was found by the sql database, the boolean exists will be true
//            boolean exists = resultSet.next();
//
//            resultSet.close();
//            statement.close();
//            connection.close();
//
//            return exists;
//
//        } catch (ClassNotFoundException | SQLException e) {
//            e.printStackTrace();
//        }
//        return false;
    }

    private boolean createAccount(String username, String password, String email, String firstname, String lastname, String birthdate) {
        // Database credentials
        String url = "jdbc:mysql://localhost:3306/canvas?useSSL=false";
        String dbUser = "root";
        String dbPassword = "root";

        String query = "INSERT INTO users (username, password, email, firstname, lastname, birthdate) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(url, dbUser, dbPassword);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            statement.setString(2, password); // Hash the password before storing it
            statement.setString(3, email);
            statement.setString(4, firstname);
            statement.setString(5, lastname);
            statement.setString(6, birthdate);

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

//        try {
//            Class.forName("com.mysql.cj.jdbc.Driver");
//            Connection connection = DriverManager.getConnection(url, dbUser, dbPassword);
//
//            // Prepare the SQL insert statement
//            String query = "INSERT INTO users (username, password, email, firstname, lastname, birthdate) VALUES (?, ?, ?, ?, ?, ?)";
//            PreparedStatement statement = connection.prepareStatement(query);
//            statement.setString(1, username);
//            statement.setString(2, password); // Ensure you hash the password before storing it
//            statement.setString(3, email);
//            statement.setString(4, firstname);
//            statement.setString(5, lastname);
//            statement.setString(6, birthdate);
//
//            int rowsAffected = statement.executeUpdate();
//
//            statement.close();
//            connection.close();
//
//            // Return true if at least one row was inserted
//            return rowsAffected > 0;
//        } catch (ClassNotFoundException | SQLException e) {
//            e.printStackTrace();
//            return false;
//        }
    }
}