package hexlet.code.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.component.JwtUtils;
import hexlet.code.domain.user.User;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;

@Component
@Slf4j
public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final JwtUtils jwtUtils;

    @Autowired
    public JWTAuthenticationFilter(final AuthenticationManager authenticationManager,
                                   final JwtUtils jwtUtils,
                                   final RequestMatcher loginRequest) {
        this.jwtUtils = jwtUtils;
        this.setAuthenticationManager(authenticationManager);
        super.setRequiresAuthenticationRequestMatcher(loginRequest);
    }

    @Override
    protected void successfulAuthentication(final HttpServletRequest request,
                                            final HttpServletResponse response,
                                            final FilterChain chain,
                                            final Authentication authResult) {
        try {
            final String jwt = jwtUtils.generateJwtToken(authResult);
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().println(jwt);
            log.info("JWT successfully generated for user {}", authResult.getName());
        } catch (JwtException | IOException e) {
            log.error("Failed to generate JWT", e);
            throw new AuthenticationServiceException("Failed to generate JWT", e);
        }
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {
        try {
            User credentials = new ObjectMapper().readValue(request.getInputStream(), User.class);
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    credentials.getEmail(), credentials.getPassword(), Collections.emptyList());
            Authentication authentication = getAuthenticationManager().authenticate(authToken);
            log.info("Authentication attempt for user {}", credentials.getEmail());
            return authentication;
        } catch (IOException e) {
            log.error("Failed to parse authentication request body", e);
            throw new AuthenticationServiceException("Failed to parse authentication request body", e);
        }
    }
}
