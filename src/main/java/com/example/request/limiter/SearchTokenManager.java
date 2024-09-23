package com.example.request.limiter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.sql.*;
import java.util.Date;


/*
MySQL table created with:

create table used_tokens (
	token VARCHAR(255) primary key,
    created_at TIMESTAMP default current_timestamp

);

set global event_scheduler = on;

create event delete_old_tokens
on schedule every 10 minute
do
delete from used_tokens
where created_at < now() - interval 1 minute;
 */

public class SearchTokenManager {

    private static final String SECRET_KEY = "root";
    private static final Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);

    public enum TokenStatus {
        VALID(0),
        TOO_EARLY(0),
        INVALID(0);

        private long timeLeft;

        TokenStatus(long timeLeft) {
            this.timeLeft = timeLeft;
        }

        public long getTimeLeft() {
            return timeLeft;
        }

        public TokenStatus setTimeLeft(long timeLeft) {
            this.timeLeft = timeLeft;
            return this;
        }

        public static TokenStatus tooEarly(long timeLeft) {
//            this.timeLeft = timeLeft;
            return TOO_EARLY.setTimeLeft(timeLeft);
        }
    }

    public static String generateToken() {

        System.out.println("Generated Token.");

        return JWT.create()
                .withIssuedAt(new Date(System.currentTimeMillis()))
//                .withNotBefore(new Date(System.currentTimeMillis() + 15000))
                .withExpiresAt(new Date(System.currentTimeMillis() + 360000))
                .withSubject("search_token")
                .sign(algorithm);
    }

    public static TokenStatus validateToken(String token) {
        try {
            // Create verifier
            JWTVerifier verifier = JWT.require(algorithm).build();

            // Decode and verify the token
            DecodedJWT jwt = verifier.verify(token);

            // Check IssuedAt
            Date IssuedAt = jwt.getIssuedAt();
            if(IssuedAt == null) {
                return TokenStatus.INVALID;
            }
            Date now = new Date();
            long delay = 1000;
            if(now.getTime() < IssuedAt.getTime() + delay) {
                return TokenStatus.tooEarly((IssuedAt.getTime() + delay) - now.getTime());
//                return TokenStatus.tooEarly((IssuedAt.getTime() + delay) - now.getTime());
            }

            String subject = jwt.getSubject();
            if(!subject.equals("search_token")) {
                return TokenStatus.INVALID;
            }

            Date exp = jwt.getExpiresAt();
            if(exp != null && exp.before(new Date())) {
                return TokenStatus.INVALID;
            }

//          Check MySQL database to ensure token hasn't been used
            if(isUsedToken(token)) {
                return TokenStatus.INVALID;
            }

            // Token is valid
            addUsedToken(token);
            return TokenStatus.VALID;

        } catch (JWTVerificationException e) {
            // Token is either invalidate or expired
            return TokenStatus.INVALID;
        } catch (Exception e) {
            return TokenStatus.INVALID;
        }
    }

//    public static boolean validateToken(String token) {
//        try {
//            // Create verifier
//            JWTVerifier verifier = JWT.require(algorithm).build();
//
//            // Decode and verify the token
//            DecodedJWT jwt = verifier.verify(token);
//
//            // Check MySQL database to ensure token hasn't been used
//            if(isUsedToken(token)) {
//               return false;
//            }
//            // Add token to the database
//            addUsedToken(token);
//
//
//            // Token is valid
//            System.out.println("Token Validated.");
//            return true;
//
//        } catch (JWTVerificationException e) {
//            // Token is either invalidate or expired
//            return false;
//        } catch (Exception e) {
//            return false;
//        }
//    }

    private static void addUsedToken(String token) throws ClassNotFoundException {
        // Database credentials
        String url = "jdbc:mysql://localhost:3306/canvas?useSSL=false";
        String dbUser = "root";
        String dbPassword = "root";

        String query = "INSERT INTO used_tokens (token) VALUES (?)";

        Class.forName("com.mysql.cj.jdbc.Driver");

        try (Connection connection = DriverManager.getConnection(url, dbUser, dbPassword);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, token);
            statement.executeUpdate(); // Use executeUpdate for INSERT

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static boolean isUsedToken(String token) throws ClassNotFoundException {
        // Database credentials
        String url = "jdbc:mysql://localhost:3306/canvas?useSSL=false";
        String dbUser = "root";
        String dbPassword = "root";

        String query = "SELECT token FROM used_tokens WHERE token = ?";

        Class.forName("com.mysql.cj.jdbc.Driver");

        try (Connection connection = DriverManager.getConnection(url, dbUser, dbPassword);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, token);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next(); // If token is found, return true
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return true; // Return false in case of an error
        }
    }
}

//        try {
//            Class.forName("com.mysql.cj.jdbc.Driver");
//            Connection connection = DriverManager.getConnection(url, dbUser, dbPassword);
//
//            PreparedStatement statement = connection.prepareStatement(query);
//            statement.setString(1, token);
//
//            ResultSet resultSet = statement.executeQuery();
//
//            boolean tokenExists = resultSet.next();
//
//            resultSet.close();
//            statement.close();
//            connection.close();
//
//            return tokenExists;
//
//        } catch (ClassNotFoundException | SQLException e) {
//            e.printStackTrace();
//        }










//package com.example.request.limiter;
//
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;
//import io.jsonwebtoken.security.SignatureException;
//
//import java.util.Date;
//
//public class LimitTokenManager {
//
//    private static final long COOLDOWN_DURATION = 15000; // 15 seconds
//    private static final String SECRET_KEY = "your-secret-key"; // Replace with a secure key
//
//    public static String generateToken() {
//        long currentTime = System.currentTimeMillis();
//        return Jwts.builder()
//                .setIssuedAt(new Date(currentTime))
//                .setExpiration(new Date(currentTime + COOLDOWN_DURATION)) // Token expires after 15 seconds
//                .claim("cooldown", currentTime + COOLDOWN_DURATION) // Custom claim for cooldown
//                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
//                .compact();
//    }
//
//    public static boolean isTokenValid(String token) {
//        try {
//            Claims claims = Jwts.parser()
//                    .setSigningKey(SECRET_KEY)
//                    .parseClaimsJws(token)
//                    .getBody();
//
//            long cooldownTime = claims.get("cooldown", Long.class);
//            long currentTime = System.currentTimeMillis();
//
//            return currentTime >= cooldownTime;
//        } catch (SignatureException e) {
//            // Invalid signature
//            return false;
//        } catch (Exception e) {
//            // Other token validation errors
//            return false;
//        }
//    }
//}






//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//public class LimitTokenManager {
//    private static final long TOKEN_VALIDITY_DURATION = 15000; // 15 seconds
//    private static final Map<String, Long> tokenTimestamps = new ConcurrentHashMap<>();
//
//    public static String generateToken() {
//        String token = java.util.UUID.randomUUID().toString();
//        long currentTime = System.currentTimeMillis();
//        tokenTimestamps.put(token, currentTime);
//        return token;
//    }
//
//    public static boolean isTokenValid(String token) {
//        Long tokenTime = tokenTimestamps.get(token);
//        if (tokenTime == null) {
//            return false; // Token doesn't exist, hence it's invalid
//        }
//
//        long currentTime = System.currentTimeMillis();
//        boolean isValid = currentTime >= tokenTime + TOKEN_VALIDITY_DURATION;
//
//        if (isValid) {
//            tokenTimestamps.remove(token); // Invalidate the token after checking
//        }
//
//        return isValid;
//    }
//}
