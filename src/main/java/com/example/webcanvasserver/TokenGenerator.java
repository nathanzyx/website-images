package com.example.webcanvasserver;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.JWTCreator.Builder;

import java.util.Date;

public class TokenGenerator {

    private static final String SECRET_KEY = "root";

    public static String generateToken(String userId) {
        System.out.println("generateToken");
        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
        return JWT.create()
                .withSubject(userId) // Store user ID in the subject claim
                .withExpiresAt(new Date(System.currentTimeMillis() + 600000)) // 1/2 hour expiration
//                .withExpiresAt(new Date(System.currentTimeMillis() + 3600000)) // 1 hour expiration
                .sign(algorithm);
    }
}
