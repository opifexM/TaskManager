package hexlet.code.config;

import com.rollbar.notifier.Rollbar;
import com.rollbar.notifier.config.Config;
import com.rollbar.spring.webmvc.RollbarSpringConfigBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

@Configuration
@ComponentScan({"hexlet.code"})
public class RollbarConfig {

    private final String rollbarToken;

    private final String activeProfile;

    public RollbarConfig(@Value("${rollbar.token}") String rollbarToken,
                         @Value("${spring.config.activate.on-profile}") String activeProfile) {
        this.rollbarToken = rollbarToken;
        this.activeProfile = activeProfile;
    }

    @Bean
    public Rollbar rollbar() {
        return new Rollbar(getRollbarConfigs(rollbarToken));
    }

    private Config getRollbarConfigs(String accessToken) {
        return RollbarSpringConfigBuilder.withAccessToken(accessToken)
                .environment("development")
                .enabled(Objects.equals(activeProfile, "prod"))
                .build();
    }
}
