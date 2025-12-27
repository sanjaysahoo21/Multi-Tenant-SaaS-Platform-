package com.example.saas.filter;

import com.example.saas.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * JWT authentication filter that extracts and validates JWT tokens
 * Sets userId, tenantId, and role as request attributes for downstream controllers
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.isTokenValid(token)) {
                String userId = jwtUtil.getUserIdFromToken(token);
                String tenantId = jwtUtil.getTenantIdFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);

                // Ensure request attributes are always present so controllers using
                // @RequestAttribute do not fail when tenantId is null (e.g., super admin)
                request.setAttribute("userId", userId);
                request.setAttribute("tenantId", tenantId != null ? tenantId : "");
                request.setAttribute("role", role);

                // Create authentication token for Spring Security
                UsernamePasswordAuthenticationToken auth = 
                    new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }
}
