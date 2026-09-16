package com.example.erp.platform.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ApiAuthFilter extends OncePerRequestFilter {

    public static final String APP_ID_ATTRIBUTE = "erp.appId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            request.setAttribute(APP_ID_ATTRIBUTE, token.substring(7));
        }
        chain.doFilter(request, response);
    }
}