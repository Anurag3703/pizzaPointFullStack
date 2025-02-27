package com.example.fullstack.security.filter;

import com.example.fullstack.security.model.UserSecurity;
import com.example.fullstack.security.util.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

    private  final JwtTokenUtil jwtTokenUtil;

    public JwtValidationFilter(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);
                if(jwtTokenUtil.validateToken(token)) {
                    Claims claims = jwtTokenUtil.getClaimsFromToken(token);
                    String subject = claims.getSubject(); //1,a@gmail.com,admin
                    String[] subArr = subject.split(",");
                    UserSecurity user = new UserSecurity();
                    user.setId(Long.parseLong(subArr[0]));
                    user.setEmail(subArr[1]);
                    user.setRole(subArr[2]);
                    //Security context
                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole());
                    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null, Collections.singletonList(authority));
                    securityContext.setAuthentication(authentication);
                    SecurityContextHolder.setContext(securityContext);
                    filterChain.doFilter(request, response);
                }else{
                    filterChain.doFilter(request, response);
                    return;
                }

        }else {
                filterChain.doFilter(request, response);
                return;
        }

    }
}
