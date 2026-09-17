package com.acme.salary.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Minimal API security for the single HR persona: requires a static bearer
 * token on API requests. This is intentionally lightweight (see
 * docs/architecture.md) - enough to show the API isn't wide open without the
 * overhead of full user management / RBAC that this single-user tool doesn't need.
 *
 * <p>Clients send: {@code Authorization: Bearer <token>} or {@code X-API-Token: <token>}.
 */
public class ApiTokenFilter extends OncePerRequestFilter {

    private final String expectedToken;

    public ApiTokenFilter(String expectedToken) {
        this.expectedToken = expectedToken;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String provided = extractToken(request);
        if (provided != null && provided.equals(expectedToken)) {
            var auth = new UsernamePasswordAuthenticationToken(
                    "hr-manager", null, AuthorityUtils.createAuthorityList("ROLE_HR"));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring("Bearer ".length()).trim();
        }
        String apiToken = request.getHeader("X-API-Token");
        return apiToken != null ? apiToken.trim() : null;
    }
}
