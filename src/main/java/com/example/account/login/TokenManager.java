package com.example.account.login;

import com.auth0.jwt.*;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import com.auth0.jwt.JWT;

import java.util.Date;

public class TokenManager {

    // SECRET_KEY to sign the token
    private static final String SECRET_KEY = "root";


    /*
    validateToken() valida

     */
    public static String validateToken(String token) {
        try {
            // Create a JWT verifier
            Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
            JWTVerifier verifier = JWT.require(algorithm).build();

            // Decode and verify the token
            DecodedJWT jwt = verifier.verify(token);

            // Extract user ID from claims
            String userId = jwt.getSubject();

            System.out.println("validateToken: Validated token, user is '" + userId + "'.");

            return userId;

        } catch (JWTVerificationException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public static String generateToken(String userId) {

        System.out.println("Generated Token for '" + userId + "'.");

        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
        return JWT.create()
                .withSubject(userId) // Store user ID in the subject claim
                .withExpiresAt(new Date(System.currentTimeMillis() + 600000)) // 1/2 hour expiration
                .sign(algorithm);
    }

}
