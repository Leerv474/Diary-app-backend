package com.leerv.diary.security;

import com.leerv.diary.exception.AuthenticationException;
import com.leerv.diary.services.JwtTokenService;
import io.micrometer.common.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class JwtFilter extends OncePerRequestFilter {
    @Autowired
    private JwtTokenService jwtTokenService;
    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        if (request.getServletPath().contains("/api/auth")) {
            filterChain.doFilter(request, response);
            return;
        }
        final String authHeaders = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String accessToken;
        final String username;

        if (authHeaders == null || !authHeaders.startsWith("Bearer")) {
            filterChain.doFilter(request, response);
            return;
        }

        accessToken = authHeaders.substring(7);

        username = jwtTokenService.extractUsername(accessToken);
        if (username == null || SecurityContextHolder.getContext().getAuthentication() != null) {
            throw new AuthenticationException("Invalid jwt token");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (!jwtTokenService.isTokenValid(accessToken, userDetails)) {
            throw new AuthenticationException("Jwt token expired");
        }
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        filterChain.doFilter(request, response);
    }
}
