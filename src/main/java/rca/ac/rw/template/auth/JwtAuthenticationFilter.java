package rca.ac.rw.template.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import rca.ac.rw.template.auth.exceptions.InvalidJwtException;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwtTokenString;
        final String principalEmail;

        String path = request.getServletPath();
        if (path.startsWith("/api/v1/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.trace("JWT Token does not begin with Bearer String or is missing. Path: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        jwtTokenString = authHeader.substring(7);

        try {
            Jwt jwtWrapper = jwtService.parseToken(jwtTokenString);
            principalEmail = jwtWrapper.getPrincipalIdentifier();

            if (principalEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(principalEmail);

                if (userDetails != null && userDetails.isEnabled()) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("Successfully authenticated user '{}' via JWT and set SecurityContext. Authorities: {}", principalEmail, userDetails.getAuthorities());
                } else {
                    log.warn("JWT valid for '{}', but UserDetails not found or account disabled.", principalEmail);
                }
            } else {
                if (principalEmail == null) log.warn("Email (principal) could not be extracted from JWT.");
            }
            filterChain.doFilter(request, response);

        } catch (InvalidJwtException ex) {
            log.warn("JWT processing error: {} for token: {}", ex.getMessage(), jwtTokenString);
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(request, response, ex);
        } catch (Exception ex) {
            log.error("Unexpected error during JWT authentication filter: {}", ex.getMessage(), ex);
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(request, response, new org.springframework.security.core.AuthenticationException("Authentication processing error", ex) {});
        }
    }
}