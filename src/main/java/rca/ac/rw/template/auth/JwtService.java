package rca.ac.rw.template.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rca.ac.rw.template.auth.exceptions.InvalidJwtException;
import rca.ac.rw.template.employee.entity.Employee;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for generating and parsing JSON Web Tokens (JWTs) for Employee authentication.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {
    private final JwtConfig config;

    /**
     * Generates an access token for the given employee.
     *
     * @param employee The employee for whom the token is generated.
     * @return A {@link Jwt} wrapper object containing the access token.
     */
    public Jwt generateAccessToken(Employee employee) {
        log.debug("Generating access token for employee: {}", employee.getEmail());
        return generateToken(employee, config.getAccessTokenExpiration());
    }

    /**
     * Generates a refresh token for the given employee.
     *
     * @param employee The employee for whom the token is generated.
     * @return A {@link Jwt} wrapper object containing the refresh token.
     */
    public Jwt generateRefreshToken(Employee employee) {
        log.debug("Generating refresh token for employee: {}", employee.getEmail());
        return generateToken(employee, config.getRefreshTokenExpiration());
    }

    /**
     * Private helper method to generate a JWT with specific claims and expiration.
     *
     * @param employee       The employee entity.
     * @param tokenExpiration The expiration time for the token in seconds.
     * @return A {@link Jwt} wrapper object.
     */
    private Jwt generateToken(Employee employee, long tokenExpiration) {
        Map<String, Object> customClaims = new HashMap<>();
        customClaims.put("email", employee.getEmail());
        customClaims.put("role", employee.getRole().name());
        customClaims.put("employeeCode", employee.getEmployeeCode());

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + tokenExpiration * 1000);

        String tokenString = Jwts.builder()
                .subject(employee.getEmail()) // Use EMAIL as subaject
                .claim("roles", employee.getRole().name()) // Use claim for roles
                .claim("employeeCode", employee.getEmployeeCode()) // Other useful claims
                .claim("userId", employee.getId().toString()) // Can still include UUID if needed elsewhere
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(config.getSecretKey())
                .compact();

        Claims claims = Jwts.parser().verifyWith(config.getSecretKey()).build().parseSignedClaims(tokenString).getPayload();
        log.trace("Generated token string: {} with claims: {}", tokenString, claims);
        return new Jwt(claims, config.getSecretKey());
    }

    /**
     * Parses the given JWT string and returns a {@link Jwt} wrapper if valid.
     *
     * @param token The JWT string.
     * @return A {@link Jwt} wrapper object.
     * @throws InvalidJwtException if the token is expired, has an invalid signature, or is malformed.
     */
    public Jwt parseToken(String token) {
        log.trace("Attempting to parse token: {}", token);
        try {
            Claims claims = getClaims(token);
            log.trace("Token parsed successfully. Claims: {}", claims);
            return new Jwt(claims, config.getSecretKey());
        } catch (ExpiredJwtException ex) {
            log.warn("Token expired: {}. Message: {}", token, ex.getMessage());
            throw new InvalidJwtException("Token has expired.");
        } catch (SignatureException ex) {
            log.warn("Invalid token signature for token: {}. Message: {}", token, ex.getMessage());
            throw new InvalidJwtException("Invalid token signature.");
        } catch (JwtException ex) {
            log.warn("Invalid JWT format or problem for token: {}. Message: {}", token, ex.getMessage());
            throw new InvalidJwtException("Invalid token format or problem: " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.warn("Invalid argument for token parsing (null/empty token?): {}", ex.getMessage());
            throw new InvalidJwtException("Invalid token (null or empty).");
        }
    }

    /**
     * Extracts claims from a JWT string.
     *
     * @param token The JWT string.
     * @return The {@link Claims} object.
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(config.getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}