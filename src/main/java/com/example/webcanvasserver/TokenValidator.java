package com.example.webcanvasserver;

import com.auth0.jwt.*;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

public class TokenValidator {
    private static final String SECRET_KEY = "root";

    public static String validateToken(String token) {
        try {
            // Create a JWT verifier
            Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
            JWTVerifier verifier = JWT.require(algorithm).build();

            // Decode and verify the token
            DecodedJWT jwt = verifier.verify(token);

            // Extract user ID from claims
            String userId = jwt.getSubject();

            System.out.println("validateToken: Validated token, user is: " + userId);

            return userId; // Assuming the user ID is stored in the subject field

        } catch (JWTVerificationException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
