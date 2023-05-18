package hexlet.code.filter;

import hexlet.code.component.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

@Component
public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final JwtUtils jwtUtils;

    @Autowired
    public JWTAuthenticationFilter(final AuthenticationManager authenticationManager, final JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
        this.setAuthenticationManager(authenticationManager);
    }

    @Override
    protected void successfulAuthentication(final HttpServletRequest request,
                                            final HttpServletResponse response,
                                            final FilterChain chain,
                                            final Authentication authResult) {
        final String jwt = jwtUtils.generateJwtToken(authResult);
        response.addHeader("Authorization", "Bearer " + jwt);
    }
}
