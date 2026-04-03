package com.flatmate.app.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String uri = request.getRequestURI();

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("[JwtFilter] {} — No Bearer token in Authorization header", uri);
            chain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = jwtUtil.validateToken(token);   // single parse
            String tokenType = claims.get("type", String.class);
            log.debug("[JwtFilter] {} — Token parsed OK. type='{}', sub='{}'", uri, tokenType, claims.getSubject());

            if (!"ACCESS".equals(tokenType)) {
                log.warn("[JwtFilter] {} — Rejected: token type is '{}', expected 'ACCESS'", uri, tokenType);
                chain.doFilter(request, response);
                return;
            }

            String userId = claims.getSubject();
            List<String> roles = claims.get("roles", List.class);

            List<SimpleGrantedAuthority> authorities = roles != null
                    ? roles.stream()
                            .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                            .collect(Collectors.toList())
                    : List.of();

            log.debug("[JwtFilter] {} — Auth set for userId='{}', roles={}", uri, userId, roles);
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (JwtException | IllegalArgumentException e) {
            log.warn("[JwtFilter] {} — Token validation failed: {}", uri, e.getMessage());
        }

        chain.doFilter(request, response);
    }
}
