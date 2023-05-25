package hexlet.code.component;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Optional;

@Component
@Slf4j
public class JwtParser {
    private final String jwtSecret;

    public JwtParser(@Value("${jwt.secret}") String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public Optional<Authentication> parseJwtToken(String token) {
        try {
            byte[] apiKeySecretBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
            SecretKey secretKey = Keys.hmacShaKeyFor(apiKeySecretBytes);

            String user = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token.replace("Bearer ", ""))
                    .getBody()
                    .getSubject();

            if (user != null) {
                log.info("User {} authenticated successfully", user);
                return Optional.of(new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>()));
            }
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired", e);
            throw new BadCredentialsException("Expired JWT token");
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported", e);
            throw new BadCredentialsException("Unsupported JWT token");
        } catch (MalformedJwtException e) {
            log.error("JWT token is malformed", e);
            throw new BadCredentialsException("Malformed JWT token");
        } catch (SignatureException e) {
            log.error("JWT signature does not match locally computed signature");
            throw new BadCredentialsException("JWT signature does not match");
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty", e);
            throw new BadCredentialsException("JWT claims string is empty");
        }
        return Optional.empty();
    }
}
