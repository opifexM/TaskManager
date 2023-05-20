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
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;

@Configuration
@EnableWebSecurity
@Slf4j
public class WebSecurityConfig {
    @Value("${base-url}")
    private String baseUrl;

    @Bean
    public AuthenticationManager authenticationManager(final AuthenticationConfiguration authenticationConfiguration) throws Exception {
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
                                        new AntPathRequestMatcher(baseUrl + "/users/**", HttpMethod.POST.toString()),
                                        new AntPathRequestMatcher(baseUrl + "/users/**", HttpMethod.GET.toString()),
                                        new NegatedRequestMatcher(new AntPathRequestMatcher(baseUrl + "/**"))
                                ).permitAll()
                                .anyRequest().authenticated()
                )
                // .authorizeHttpRequests(
                //         matcherRegistry -> matcherRegistry
                //                 .requestMatchers(HttpMethod.GET, baseUrl + "/users/**").permitAll()
                //                 .requestMatchers(HttpMethod.POST, baseUrl + "/users/**").permitAll()
                //                 // new NegatedRequestMatcher(new AntPathRequestMatcher(baseUrl + "/**"))
                //                 // .requestMatchers(baseUrl + "/users/**").permitAll()
                //                 // .requestMatchers("/**").permitAll()
                //                 .anyRequest().authenticated()
                // )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthorizationFilter, JWTAuthenticationFilter.class)
                // .formLogin(formLogin -> formLogin
                //                 // .usernameParameter("username")
                //                 // .passwordParameter("password")
                //                 // .loginPage("/authentication/login")
                //                 // .failureUrl("/authentication/login?failed")
                //                 .loginProcessingUrl(baseUrl + "/api/login")
                // );
        ;
        return http.build();
    }
}
