package com.example.login;

public class LoginService {
    public static String handleLogin(String userId) {
        return TokenManager.generateToken(userId);
    }
}