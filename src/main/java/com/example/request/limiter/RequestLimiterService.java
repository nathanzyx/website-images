package com.example.request.limiter;


import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

@Path("/limit_token")
public class RequestLimiterService {
    @POST
    @Produces("application/json")
    public Response getToken() {
        String token = SearchTokenManager.generateToken();
        return Response.ok().entity("{\"token\": \"" + token + "\"}").build();
    }

//    @GET
//    @Path("/validateToken")
//    @Produces("application/json")
//    public Response validateToken(@QueryParam("token") String token) {
//        SearchTokenManager.TokenStatus status = SearchTokenManager.validateToken(token);
//        switch (status) {
//            case VALID:
//                return Response.ok()
//                        .entity("{\"valid\": true}").build();
//            case TOO_EARLY:
//                return Response.status(Response.Status.TOO_MANY_REQUESTS)
//                        .entity("{\"valid\": false, \"reason\": \"TOO_EARLY\"}").build();
//            case INVALID:
//                return Response.status(Response.Status.UNAUTHORIZED)
//                        .entity("{\"valid\": false, \"reason\": \"EXPIRED\"}").build();
//            default:
//                return Response.status(Response.Status.UNAUTHORIZED)
//                        .entity("{\"valid\": false, \"reason\": \"EXPIRED\"}").build();
//        }
//    }
}














//import com.example.account.login.TokenManager;
//import jakarta.json.Json;
//import jakarta.json.JsonObject;
//import jakarta.ws.rs.*;
//import jakarta.ws.rs.core.Response;
//
//import java.io.StringReader;
//import java.sql.*;

//@Path("/limit_token")
//public class RequestLimiterService {
//    @POST
//    @Produces("application/json")
//    public Response login() {
//        String token = LimitTokenManager.generateToken();
//
//        return Response.ok().entity("{\"token\": \"" + token + "\"}").build();
//    }
//
//
//
//    @GET
//    @Path("/validateToken")
//    @Produces("application/json")
//    public Response validateToken(@QueryParam("token") String token) {
//        if (LimitTokenManager.isTokenValid(token)) {
//            return Response.ok().entity("{\"valid\": true}").build();
//        } else {
//            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"valid\": false}").build();
//        }
//    }
//}