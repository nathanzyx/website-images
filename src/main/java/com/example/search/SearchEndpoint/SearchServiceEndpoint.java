package com.example.search.SearchEndpoint;

import com.example.request.limiter.SearchTokenManager;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.io.StringReader;

@Path("/api")
public class SearchServiceEndpoint {

    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public Response makeSearch(@HeaderParam("Authorization") String authHeader,String jsonBody) {
        // Get token
        String token = null;
        if(authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"valid\": false, \"reason\": \"INVALID\"}").build();
        }

        // Check Token
        SearchTokenManager.TokenStatus searchTokenValidity = SearchTokenManager.validateToken(token);
        if(searchTokenValidity != SearchTokenManager.TokenStatus.VALID) {
            return SearchTokenValidity(searchTokenValidity);
        }

        // Get coaster name and element data from user
        JsonObject jsonObject = Json.createReader(new StringReader(jsonBody)).readObject();

        // Check Parameters


        // Execute Search




        return Response.ok().build();
    }

    public Response SearchTokenValidity(SearchTokenManager.TokenStatus validity) {
        if (validity == SearchTokenManager.TokenStatus.TOO_EARLY) {

            long timeLeft = validity.getTimeLeft(); // Get time left for next search

            return Response.status(Response.Status.TOO_MANY_REQUESTS)
                    .entity("{\"valid\": false, \"reason\": \"TOO_EARLY\", \"time\": " + timeLeft/1000 + "}").build();
        }

        return Response.status(Response.Status.UNAUTHORIZED)
                .entity("{\"valid\": false, \"reason\": \"INVALID\"}").build();

    }
}
