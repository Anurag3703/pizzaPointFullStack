package com.example.fullstack.security.util;
import com.example.fullstack.security.model.UserSecurity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

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
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) //
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
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
        return false;
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(getSignKey()).build().parseClaimsJws(token).getBody();
    }

}
