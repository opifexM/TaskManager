package hexlet.code.filter;

import hexlet.code.component.JwtParser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
@Slf4j
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private final JwtParser jwtParser;

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, JwtParser jwtParser) {
        super(authenticationManager);
        this.jwtParser = jwtParser;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            log.warn("No Authorization header or does not start with Bearer");
            chain.doFilter(request, response);
            return;
        }

        Optional<Authentication> authentication = jwtParser.parseJwtToken(request.getHeader("Authorization"));
        if (authentication.isPresent()) {
            log.info("Authentication successful for user: {}", authentication.get().getName());
            SecurityContextHolder.getContext().setAuthentication(authentication.get());
        } else {
            log.warn("Failed to authenticate user");
        }
        chain.doFilter(request, response);
        log.info("Filtering completed for request: {}", request.getRequestURI());
    }
}

