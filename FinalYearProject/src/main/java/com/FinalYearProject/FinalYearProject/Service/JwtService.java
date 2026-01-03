package com.FinalYearProject.FinalYearProject.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

//this class is implementation the jwt filter meaning
/**
 * JwtService - JSON Web Token Generation and Validation Service
 * PURPOSE: Service for JWT token operations - generation, parsing, validation, and claim extraction. Implements stateless authentication for Spring Security.
 * TOKEN GENERATION: Generates JWT tokens with email as subject, role claim, 5-hour expiration, and HMAC SHA256 signing. Uses dynamic secret key generation at startup.
 * SECRET KEY MANAGEMENT: Generates new HMAC SHA256 key on application startup using Java KeyGenerator. Key printed to console for debugging (remove in production).
 * TOKEN VALIDATION: validateToken checks token expiration, email match with UserDetails, and role authorization. isTokenExpiredOrThrow throws exception for expired tokens.
 * CLAIM EXTRACTION: extractUserEmail gets subject (email). extractUserRole extracts custom "role" claim. extractExpiration gets token expiry time.
 * JWT LIBRARY: Uses io.jsonwebtoken (JJWT) for standards-compliant JWT operations. Base64 encoding for secret key storage.
 * TOKEN STRUCTURE: Contains claims map with role, subject (email), issuedAt, expiration. Signed with HMAC SHA256 for integrity verification.
 * SECURITY CONSIDERATIONS: 5-hour token expiration balances security and usability. Dynamic key generation means all tokens invalid on restart (forces re-login).
 * INTEGRATION: Used by JwtFilter for request authentication. UserService uses jwtToken() for login token generation. Extracts tokens from Authorization header.
 * PRODUCTION NOTES: Secret key should be from configuration (not regenerated). Expiration should be configurable. Consider refresh token implementation.
 * ERROR HANDLING: Throws RuntimeException for expired/invalid tokens. JWT parsing exceptions propagate for proper error responses.
 */
@Service
public class JwtService  {
    private String secretKey;
    public JwtService(){
    try {
        KeyGenerator keyGenerator= KeyGenerator.getInstance("HmacSHA256");
        SecretKey sk=keyGenerator.generateKey();
        secretKey= Base64.getEncoder().encodeToString(sk.getEncoded());
        System.out.println("========================================");
        System.out.println("🧩 New Secret Key Generated at Startup:");
        System.out.println(secretKey);// every user must reLogin after restart
        System.out.println("========================================");
    }
    catch (NoSuchAlgorithmException e){
        throw new RuntimeException(e);
    }
    }
    public SecretKey getKey(){
        byte [] KeyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(KeyBytes);
    }
    public String jwtToken(String email ,String role){
        Map<String ,Object> claims = new HashMap<>();
        claims.put("role",role);
        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() +1000*60*60*5))
                .and()
                .signWith(getKey())
                .compact();
    }

    public String extractUserEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    private <T> T extractClaim(String token, Function<Claims ,T> claimResolver){
        final Claims claims = extractAllClaims(token);
            return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUserEmail(token);
        return (
                username.equals(
                        userDetails.getUsername()
                )
                        && !isTokenExpired(token)
                        && userDetails.getAuthorities()
                        .contains(
                                new SimpleGrantedAuthority(extractUserRole(token)
                                )
                        )
        );
    }
    private boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    public String extractUserRole(String token){
        return extractClaim(token,claims -> claims.get("role", String.class));
    }
    public void isTokenExpiredOrThrow(String token){
        if (isTokenExpired(token)){
            throw new RuntimeException("JWT token expired");
        }
    }
}
