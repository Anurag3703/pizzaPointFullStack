package com.example.fullstack.security.filter;

import com.example.fullstack.security.model.UserSecurity;
import com.example.fullstack.security.util.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtValidationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtValidationFilter.class);
    private final JwtTokenUtil jwtTokenUtil;

    public JwtValidationFilter(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String header = request.getHeader("Authorization");

        logger.debug("Processing request: {} {}", request.getMethod(), requestURI);
        logger.debug("Authorization header: {}", header != null ? "Present" : "Missing");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            logger.debug("Token extracted: {}", token.substring(0, Math.min(token.length(), 10)) + "...");

            try {
                if (jwtTokenUtil.validateToken(token)) {
                    Claims claims = jwtTokenUtil.getClaimsFromToken(token);
                    String subject = claims.getSubject();
                    logger.debug("Token valid. Subject: {}", subject);

                    String[] subArr = subject.split(",");
                    UserSecurity user = new UserSecurity();
                    user.setId(Long.parseLong(subArr[0]));
                    user.setEmail(subArr[1]);
                    user.setRole(subArr[2]);

                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole());
                    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(user, null, Collections.singletonList(authority));
                    securityContext.setAuthentication(authentication);
                    SecurityContextHolder.setContext(securityContext);

                    logger.debug("Authentication set for user: {}", user.getEmail());
                } else {
                    logger.warn("Invalid JWT token for request: {}", requestURI);
                }
            } catch (Exception e) {
                logger.error("Error processing JWT token: {}", e.getMessage());
            }
        } else {
            logger.debug("No Authorization header found for request: {}", requestURI);
        }

        filterChain.doFilter(request, response);
    }
}