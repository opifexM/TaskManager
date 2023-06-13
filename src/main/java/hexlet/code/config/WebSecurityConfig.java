package hexlet.code.config;

import hexlet.code.filter.JWTAuthenticationFilter;
import hexlet.code.filter.JwtAuthorizationFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatchers;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity()
@Slf4j
public class WebSecurityConfig {

    private final String baseUrl;

    public WebSecurityConfig(@Value("${base-url}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Bean
    public RequestMatcher apiLoginMatcher() {
        return RequestMatchers.allOf(
                new AntPathRequestMatcher(baseUrl + "/login", HttpMethod.POST.toString()));
    }

    @Bean
    public AuthenticationManager authenticationManager(final AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity http,
                                           final JWTAuthenticationFilter jwtAuthenticationFilter,
                                           final JwtAuthorizationFilter jwtAuthorizationFilter) throws Exception {
        log.info("config http security");
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        authorize -> authorize
                                .requestMatchers(
                                        RequestMatchers.anyOf(
                                                new AntPathRequestMatcher(baseUrl + "/users",
                                                        HttpMethod.POST.toString()),
                                                new AntPathRequestMatcher(baseUrl + "/users",
                                                        HttpMethod.GET.toString()),
                                                new AntPathRequestMatcher(baseUrl + "/login",
                                                        HttpMethod.POST.toString())
                                        ),
                                        RequestMatchers.not(new AntPathRequestMatcher(baseUrl + "/**"))
                                ).permitAll()
                                .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthorizationFilter, JWTAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info("Creating Argon2PasswordEncoder");
        return new Argon2PasswordEncoder(16, 32, 1, 1 << 14, 2);
    }
}
