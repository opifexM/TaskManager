package hexlet.code.component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
@Slf4j
public class JwtUtils {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpiration;

    public String generateJwtToken(final Authentication authentication) {

        final org.springframework.security.core.userdetails.User userPrincipal
                = (org.springframework.security.core.userdetails.User) authentication.getPrincipal();

        byte[] apiKeySecretBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        SecretKey secretKey = Keys.hmacShaKeyFor(apiKeySecretBytes);

        String jwt = Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plusMillis(jwtExpiration)))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();

        log.info("Generated JWT for user {}", userPrincipal.getUsername());
        return jwt;
    }
}
