package rca.ac.rw.template.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import rca.ac.rw.template.common.enums.Role; // Assuming Role enum is in common.enums

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

public class Jwt {
    private final Claims claims;
    private final SecretKey secretKey; // Keep for toString()

    public Jwt(Claims claims, SecretKey secretKey) {
        this.claims = claims;
        this.secretKey = secretKey;
    }

    public boolean isExpired() {
        return claims.getExpiration().before(new Date());
    }

    /**
     * Gets the principal identifier from the JWT subject.
     * This should be the employee's email as set in JwtService.
     * @return String email (or whatever is set as subject).
     */
    public String getPrincipalIdentifier() {
        return claims.getSubject();
    }

    /**
     * Gets the Role from the JWT "roles" claim.
     * Note: JwtService sets a single role string in a claim named "roles".
     * @return Role enum.
     */
    public Role getRole() {
        String roleString = claims.get("roles", String.class); // Matching claim from JwtService
        return (roleString != null) ? Role.valueOf(roleString) : null;
    }

    /**
     * Gets the technical User ID (UUID) from the "userId" claim.
     * @return UUID of the user.
     */
    public UUID getTechnicalId() {
        String userIdStr = claims.get("userId", String.class);
        return (userIdStr != null) ? UUID.fromString(userIdStr) : null;
    }

    /**
     * Gets the Employee Code from the "employeeCode" claim.
     * @return String employee code.
     */
    public String getEmployeeCodeClaim() { // Renamed to avoid confusion with a potential getEmployeeCode() method
        return claims.get("employeeCode", String.class);
    }


    @Override
    public String toString() {
        // This rebuilds the token string from claims.
        // Alternatively, JwtService could pass the generated token string to the Jwt constructor
        // to avoid rebuilding, but this works.
        return Jwts.builder()
                .claims(this.claims)
                .signWith(this.secretKey)
                .compact();
    }

    public Claims getAllClaims(){
        return this.claims;
    }
}