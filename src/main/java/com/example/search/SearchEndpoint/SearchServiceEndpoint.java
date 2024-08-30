package com.example.search.SearchEndpoint;

import com.example.request.limiter.SearchTokenManager;
import jakarta.json.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.lang.Math;

//import javax.json.Json;
//import javax.json.JsonArray;
//import javax.json.JsonObject;

import java.io.StringReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Path("/api")
public class SearchServiceEndpoint {

    public class ImageResult {
        private int imageId;
        private byte[] imageData;
        private String coasterName;

        private String imageName;

        public int getImageId() {
            return this.imageId;
        }

        public void setImageId(int imageId) {
            this.imageId = imageId;
        }

        // Getter and setter for imageData
        public byte[] getImageData() {
            return imageData;
        }

        public void setImageData(byte[] imageData) {
            this.imageData = imageData;
        }

        public void setImageName(String imageName) {
            this.imageName = imageName;
        }

        // Getter and setter for coasterName
        public String getCoasterName() {
            return coasterName;
        }

        public void setCoasterName(String coasterName) {
            this.coasterName = coasterName;
        }

        public JsonObject toJsonObject() {
            JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder()
                    .add("imageId", imageId)
                    .add("coasterName", coasterName)
                    .add("imageData", Base64.getEncoder().encodeToString(imageData)); // Encode byte array to Base64

            return jsonObjectBuilder.build();
        }
    }

    String url = "jdbc:mysql://localhost:3306/canvas?useSSL=false";
    String dbUser = "root";
    String dbPassword = "root";

    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public Response makeSearch(@HeaderParam("Authorization") String authHeader,String jsonBody) throws ClassNotFoundException {
        // Get token
        String token = null;
        if(authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"submit\": \"Please try again\"}").build();
        }

        // Check Token
        SearchTokenManager.TokenStatus searchTokenValidity = SearchTokenManager.validateToken(token);
        if(searchTokenValidity != SearchTokenManager.TokenStatus.VALID) {
            return SearchTokenValidity(searchTokenValidity);
        }

        // Get coaster name and element data from user
        JsonObject jsonObject = Json.createReader(new StringReader(jsonBody)).readObject();
        String name = jsonObject.getString("name");
        JsonArray elementsArray = jsonObject.getJsonArray("elements");
        int limit = 10;

        System.out.println(name);
        System.out.println(elementsArray);

        List<Integer> excludedImageIds = new ArrayList<>();
        JsonArray excludedImageIdsArray = jsonObject.getJsonArray("excludedImageIds");
        if (excludedImageIdsArray != null) {
            for (JsonValue value : excludedImageIdsArray) {
                if (value.getValueType() == JsonValue.ValueType.NUMBER) {
                    excludedImageIds.add(((JsonNumber) value).intValue());
                }
            }
        }

        System.out.println("excludedImageIds: " + excludedImageIds);

        // Initialize a list for coaster image results
        List<ImageResult> imageResults = new ArrayList<>();

        // Get similar coaster names from database
        List<String> similarNames = new ArrayList<>();
        if(!name.isEmpty()) {
            int name_distance = (int)Math.round(name.length() * .333);
            similarNames = getSimilarNames(name, name_distance);
        }
        if(!name.isEmpty() && similarNames.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"coaster\": \"No matching coaster\"}").build();
        }
        if(name.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"coaster\": \"Must include coaster\"}").build();
        }

        System.out.println("SimilarNames: " + similarNames);

        // Get similar elements form database
        List<String> similarElements = new ArrayList<>();
        for (int i = 0; i < elementsArray.size(); i++) {
            int element_distance = (int)Math.round((elementsArray.getString(i)).length() * .333);

            String element = elementsArray.getString(i);
            List<String> similarElementNames = getSimilarElements(element, element_distance);
            similarElements.addAll(similarElementNames);
        }
        if(!elementsArray.isEmpty() && similarElements.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"element\": \"No matching elements\"}").build();
        }

        System.out.println("SimilarElements: " + similarElements);

        // Determine the search parameters
        if (similarNames.isEmpty() && elementsArray.isEmpty()) {
            // If neither name nor elements are provided, return an empty response or an error
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"submit\": \"No search parameters provided\"}").build();
        }

        // Execute Search
        imageResults = searchImages(similarNames, similarElements, excludedImageIds, limit);

        System.out.println("imageResults: " + imageResults);

        if(imageResults.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"load\": \"No more results!\"}").build();
        }

        // Convert imageResults to JSON
        JsonArrayBuilder resultsArrayBuilder = Json.createArrayBuilder();
        for (ImageResult imageResult : imageResults) {
            resultsArrayBuilder.add(imageResult.toJsonObject());
        }

        JsonObject responseObject = Json.createObjectBuilder()
                .add("images", resultsArrayBuilder.build())
                .build();

        return Response.ok(responseObject).build();
    }

    private List<String> getSimilarElements(String element, int distance) throws ClassNotFoundException {
        String query = "SELECT element_name FROM elements WHERE LEVENSHTEIN(element_name, ?) <= ? ORDER BY LEVENSHTEIN(element_name, ?) LIMIT 3";

        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection connection = DriverManager.getConnection(url, dbUser, dbPassword);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, element);
            statement.setString(2, Integer.toString(distance));
            statement.setString(3, element);
            ResultSet results = statement.executeQuery();

            List<String> elements = new ArrayList<>();
            while(results.next()) {
                elements.add(results.getString("element_name"));
            }

            return elements;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>(); // Return false in case of an error
    }
    private List<ImageResult> searchImages(List<String> names, List<String> elements, List<Integer> excludedImageIds, int limit) throws ClassNotFoundException {
        StringBuilder queryBuilder = new StringBuilder(
                "SELECT DISTINCT i.image_id, i.image_data, i.image_name, c.coaster_name " +
                        "FROM images i " +
                        "LEFT JOIN image_coasters ic ON i.image_id = ic.image_id " +
                        "LEFT JOIN coasters c ON ic.coaster_id = c.coaster_id " +
                        "LEFT JOIN image_elements ie ON i.image_id = ie.image_id " +
                        "LEFT JOIN elements e ON ie.element_id = e.element_id " +
                        "WHERE 1 = 1 "
        );

        // Add filtering for coaster names
        if (names != null && !names.isEmpty()) {
            queryBuilder.append("AND c.coaster_name IN (");
            for (int i = 0; i < names.size(); i++) {
                queryBuilder.append("?");
                if (i < names.size() - 1) {
                    queryBuilder.append(", ");
                }
            }
            queryBuilder.append(") ");
        }

        // Add filtering for elements
        if (elements != null && !elements.isEmpty()) {
            queryBuilder.append("AND e.element_name IN (");
            for (int i = 0; i < elements.size(); i++) {
                queryBuilder.append("?");
                if (i < elements.size() - 1) {
                    queryBuilder.append(", ");
                }
            }
            queryBuilder.append(") ");
        }

        // Exclude previously returned image IDs if any
        if (excludedImageIds != null && !excludedImageIds.isEmpty()) {
            queryBuilder.append("AND i.image_id NOT IN (");
            for (int i = 0; i < excludedImageIds.size(); i++) {
                queryBuilder.append("?");
                if (i < excludedImageIds.size() - 1) {
                    queryBuilder.append(", ");
                }
            }
            queryBuilder.append(") ");
        }

        // Group by image_id to ensure unique results
        queryBuilder.append("GROUP BY i.image_id ");

        // Ensure all specified elements are present
        if (elements != null && !elements.isEmpty()) {
            queryBuilder.append("HAVING COUNT(DISTINCT e.element_name) = ? ");
        }

        // Limit the number of results
        queryBuilder.append("LIMIT ?");

        String query = queryBuilder.toString();

        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection connection = DriverManager.getConnection(url, dbUser, dbPassword);
             PreparedStatement statement = connection.prepareStatement(query)) {

            int index = 1;

            // Set coaster names
            if (names != null && !names.isEmpty()) {
                for (String coasterName : names) {
                    statement.setString(index++, coasterName);
                }
            }

            // Set element filters
            if (elements != null && !elements.isEmpty()) {
                for (String element : elements) {
                    statement.setString(index++, element);
                }
            }

            // Set excluded image IDs
            if (excludedImageIds != null && !excludedImageIds.isEmpty()) {
                for (Integer imageId : excludedImageIds) {
                    statement.setInt(index++, imageId);
                }
            }

            // Set the count of required elements
            if (elements != null && !elements.isEmpty()) {
                statement.setInt(index++, elements.size());
            }

            // Set the limit
            statement.setInt(index, limit);

            ResultSet results = statement.executeQuery();
            List<ImageResult> imageResults = new ArrayList<>();
            while (results.next()) {
                ImageResult image = new ImageResult();
                image.setImageId(results.getInt("image_id"));
                image.setImageData(results.getBytes("image_data"));
                image.setImageName(results.getString("image_name"));
                image.setCoasterName(results.getString("coaster_name"));
                imageResults.add(image);
            }

            return imageResults;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new ArrayList<>(); // Return an empty list in case of an error








//        StringBuilder queryBuilder = new StringBuilder(
//                "SELECT i.image_id, i.image_data, i.image_name, c.coaster_name " +
//                        "FROM images i " +
//                        "LEFT JOIN image_coasters ic ON i.image_id = ic.image_id " +
//                        "LEFT JOIN coasters c ON ic.coaster_id = c.coaster_id " +
//                        "LEFT JOIN image_elements ie ON i.image_id = ie.image_id " +
//                        "LEFT JOIN elements e ON ie.element_id = e.element_id " +
//                        "WHERE 1 = 1 "
//        );
//
//        // Add filtering for image names
//        if (names != null && !names.isEmpty()) {
//            queryBuilder.append("AND i.image_name IN (");
//            for (int i = 0; i < names.size(); i++) {
//                queryBuilder.append("?");
//                if (i < names.size() - 1) {
//                    queryBuilder.append(", ");
//                }
//            }
//            queryBuilder.append(") ");
//        }
//
//        // Add filtering for elements
//        if (elements != null && !elements.isEmpty()) {
//            queryBuilder.append("AND e.element_name IN (");
//            for (int i = 0; i < elements.size(); i++) {
//                queryBuilder.append("?");
//                if (i < elements.size() - 1) {
//                    queryBuilder.append(", ");
//                }
//            }
//            queryBuilder.append(") ");
//        }
//
//        // Exclude previously returned image IDs if any
//        if (excludedImageIds != null && !excludedImageIds.isEmpty()) {
//            queryBuilder.append("AND i.image_id NOT IN (");
//            for (int i = 0; i < excludedImageIds.size(); i++) {
//                queryBuilder.append("?");
//                if (i < excludedImageIds.size() - 1) {
//                    queryBuilder.append(", ");
//                }
//            }
//            queryBuilder.append(") ");
//        }
//
//        // Limit the number of results
//        queryBuilder.append("LIMIT ?");
//
//        String query = queryBuilder.toString();
//
//        Class.forName("com.mysql.cj.jdbc.Driver");
//        try (Connection connection = DriverManager.getConnection(url, dbUser, dbPassword);
//             PreparedStatement statement = connection.prepareStatement(query)) {
//
//            int index = 1;
//
//            // Set image names
//            if (names != null && !names.isEmpty()) {
//                for (String name : names) {
//                    statement.setString(index++, name);
//                }
//            }
//
//            // Set element filters
//            if (elements != null && !elements.isEmpty()) {
//                for (String element : elements) {
//                    statement.setString(index++, element);
//                }
//            }
//
//            // Set excluded image IDs
//            if (excludedImageIds != null && !excludedImageIds.isEmpty()) {
//                for (Integer imageId : excludedImageIds) {
//                    statement.setInt(index++, imageId);
//                }
//            }
//
//            // Set the limit
//            statement.setInt(index, limit);
//
//            ResultSet results = statement.executeQuery();
//            List<ImageResult> imageResults = new ArrayList<>();
//            while (results.next()) {
//                ImageResult image = new ImageResult();
//                image.setImageId(results.getInt("image_id"));
//                image.setImageData(results.getBytes("image_data"));
//                image.setCoasterName(results.getString("coaster_name")); // Update to use coaster_name
//                imageResults.add(image);
//            }
//
//            return imageResults;
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//
//        return new ArrayList<>(); // Return an empty list in case of an error








//        StringBuilder queryBuilder = new StringBuilder(
//                "SELECT i.image_id, i.image_data, i.image_name " +
//                        "FROM images i " +
//                        "JOIN coasters c ON i.coaster_name = c.coaster_name " +
//                        "WHERE i.image_name IN ("
//        );
//
//        // Add placeholders for coaster names
//        for (int i = 0; i < names.size(); i++) {
//            queryBuilder.append("?");
//            if (i < names.size() - 1) {
//                queryBuilder.append(", ");
//            }
//        }
//
//        // Add elements filtering if any
//        if (elements != null && !elements.isEmpty()) {
//            queryBuilder.append(") AND (");
//            for (int i = 0; i < elements.size(); i++) {
//                queryBuilder.append("FIND_IN_SET(?, i.elements) > 0");
//                if (i < elements.size() - 1) {
//                    queryBuilder.append(" OR ");
//                }
//            }
//            queryBuilder.append(")");
//        } else {
//            queryBuilder.append(")");
//        }
//
//        // Exclude previously returned image IDs if any
//        if (excludedImageIds != null && !excludedImageIds.isEmpty()) {
//            queryBuilder.append(" AND i.image_id NOT IN (");
//            for (int i = 0; i < excludedImageIds.size(); i++) {
//                queryBuilder.append("?");
//                if (i < excludedImageIds.size() - 1) {
//                    queryBuilder.append(", ");
//                }
//            }
//            queryBuilder.append(")");
//        }
//
//        // Limit the number of results
//        queryBuilder.append(" LIMIT ?");
//
//        String query = queryBuilder.toString();
//
//        Class.forName("com.mysql.cj.jdbc.Driver");
//        try (Connection connection = DriverManager.getConnection(url, dbUser, dbPassword);
//             PreparedStatement statement = connection.prepareStatement(query)) {
//
//            // Set coaster names
//            int index = 1;
//            for (String name : names) {
//                statement.setString(index++, name);
//            }
//
//            // Set element filters
//            if (elements != null && !elements.isEmpty()) {
//                for (String element : elements) {
//                    statement.setString(index++, element);
//                }
//            }
//
//            // Set excluded image IDs
//            if (excludedImageIds != null && !excludedImageIds.isEmpty()) {
//                for (Integer imageId : excludedImageIds) {
//                    statement.setInt(index++, imageId);
//                }
//            }
//
//            // Set the limit
//            statement.setInt(index, limit);
//
//            ResultSet results = statement.executeQuery();
//            List<ImageResult> imageResults = new ArrayList<>();
//            while (results.next()) {
//                ImageResult image = new ImageResult();
//                image.setImageId(results.getInt("image_id"));
//                image.setImageData(results.getBytes("image_data"));
//                image.setCoasterName(results.getString("coaster_name"));
//                imageResults.add(image);
//            }
//
//            return imageResults;
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//
//        return new ArrayList<>(); // Return an empty list in case of an error
    }

    private List<String> getSimilarNames(String name, int distance) throws ClassNotFoundException {
        String query = "SELECT coaster_name FROM coasters WHERE LEVENSHTEIN(coaster_name, ?) <= ? ORDER BY LEVENSHTEIN(coaster_name, ?) LIMIT 3";

        Class.forName("com.mysql.cj.jdbc.Driver");

        try (Connection connection = DriverManager.getConnection(url, dbUser, dbPassword);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, name);
            statement.setString(2, Integer.toString(distance));
            statement.setString(3, name);
            ResultSet results = statement.executeQuery();

            List<String> names = new ArrayList<>();
            while(results.next()) {
                names.add(results.getString("coaster_name"));
            }

            return names;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>(); // Return false in case of an error
    }

    public Response SearchTokenValidity(SearchTokenManager.TokenStatus validity) {
        if (validity == SearchTokenManager.TokenStatus.TOO_EARLY) {

            long timeLeft = validity.getTimeLeft(); // Get time left for next search

            return Response.status(Response.Status.TOO_MANY_REQUESTS)
                    .entity("{\"submit\": \"Please wait \", \"time\": " + timeLeft/1000 + "}").build();
        }

        return Response.status(Response.Status.UNAUTHORIZED)
                .entity("{\"submit\": \"Invalid request\"}").build();

    }
}
