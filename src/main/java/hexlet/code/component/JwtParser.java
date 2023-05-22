package hexlet.code.component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

@Component
@Slf4j
public class JwtParser {
    @Value("${jwt.secret}")
    private String jwtSecret;

    public Authentication parseJwtToken(String token) {
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
                return new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>());
            }
        } catch (Exception e) {
            log.error("Can't extract login data from request", e);
            throw new BadCredentialsException("Can't extract login data from request");
        }
        return null;
    }
}
