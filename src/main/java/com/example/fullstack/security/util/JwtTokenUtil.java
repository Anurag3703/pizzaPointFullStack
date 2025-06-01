package com.example.fullstack.security.util;

import com.example.fullstack.security.model.UserSecurity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtTokenUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenUtil.class);

    @Value("${app.jwt.secret}")
    private String SECRET;

    @Value("${app.jwt.expiration}")
    private Long EXPIRATION;

    public String generateToken(UserSecurity user) {
        long expiration = EXPIRATION; // default expiration

        // Longer expiry for admins
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            expiration = 7 * 24 * 60 * 60 * 1000L; // 7 days in milliseconds
        }

        return Jwts.builder()
                .setSubject(user.getId() + "," + user.getUsername() + "," + user.getRole())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(getSignKey()).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
        }
        return false;
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(getSignKey()).build().parseClaimsJws(token).getBody();
    }

    // New methods for enhanced token validation

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            log.debug("Token is expired: {}", e.getMessage());
            return true;
        } catch (Exception e) {
            log.error("Error checking token expiration: {}", e.getMessage());
            return true; // Consider invalid tokens as expired
        }
    }

    /**
     * Get expiration date from token
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * Get issued date from token
     */
    public Date getIssuedAtDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getIssuedAt);
    }

    /**
     * Get username from token (extracted from subject)
     */
    public String getUsernameFromToken(String token) {
        String subject = getClaimFromToken(token, Claims::getSubject);
        if (subject != null && subject.contains(",")) {
            String[] parts = subject.split(",");
            return parts.length > 1 ? parts[1] : null; // username is second part
        }
        return subject;
    }

    /**
     * Get user ID from token (extracted from subject)
     */
    public String getUserIdFromToken(String token) {
        String subject = getClaimFromToken(token, Claims::getSubject);
        if (subject != null && subject.contains(",")) {
            String[] parts = subject.split(",");
            return parts.length > 0 ? parts[0] : null; // ID is first part
        }
        return null;
    }

    /**
     * Get user role from token (extracted from subject)
     */
    public String getRoleFromToken(String token) {
        String subject = getClaimFromToken(token, Claims::getSubject);
        if (subject != null && subject.contains(",")) {
            String[] parts = subject.split(",");
            return parts.length > 2 ? parts[2] : null; // role is third part
        }
        return null;
    }

    /**
     * Generic method to get a claim from token
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Get time remaining until token expires (in milliseconds)
     */
    public long getTimeUntilExpiration(String token) {
        try {
            Date expirationDate = getExpirationDateFromToken(token);
            return Math.max(0, expirationDate.getTime() - System.currentTimeMillis());
        } catch (Exception e) {
            log.error("Error calculating time until expiration: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Validate token with detailed exception handling
     */
    public TokenValidationResult validateTokenWithDetails(String token) {
        try {
            Jwts.parser().setSigningKey(getSignKey()).build().parseClaimsJws(token);

            // Check if expired
            if (isTokenExpired(token)) {
                return new TokenValidationResult(false, "Token is expired", TokenValidationResult.ErrorType.EXPIRED);
            }

            return new TokenValidationResult(true, "Token is valid", null);

        } catch (ExpiredJwtException e) {
            return new TokenValidationResult(false, "Token is expired", TokenValidationResult.ErrorType.EXPIRED);
        } catch (MalformedJwtException e) {
            return new TokenValidationResult(false, "Malformed JWT token", TokenValidationResult.ErrorType.MALFORMED);
        } catch (SignatureException e) {
            return new TokenValidationResult(false, "Invalid JWT signature", TokenValidationResult.ErrorType.INVALID_SIGNATURE);
        } catch (Exception e) {
            log.error("Token validation error: {}", e.getMessage());
            return new TokenValidationResult(false, "Invalid token: " + e.getMessage(), TokenValidationResult.ErrorType.OTHER);
        }
    }

    // Inner class for detailed validation results
    @Getter
    public static class TokenValidationResult {
        // Getters
        private final boolean valid;
        private final String message;
        private final ErrorType errorType;

        public enum ErrorType {
            EXPIRED, MALFORMED, INVALID_SIGNATURE, OTHER
        }

        public TokenValidationResult(boolean valid, String message, ErrorType errorType) {
            this.valid = valid;
            this.message = message;
            this.errorType = errorType;
        }

    }
}