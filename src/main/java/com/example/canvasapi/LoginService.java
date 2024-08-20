package com.example.canvasapi;

import com.example.webcanvasserver.TokenGenerator;

public class LoginService {
    public static String handleLogin(String userId) {
        return TokenGenerator.generateToken(userId);
    }
}