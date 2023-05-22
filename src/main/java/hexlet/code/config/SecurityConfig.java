package hexlet.code.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    // Основываясь на рекомендациях для Spring Security 6 экземпляр Argon2PasswordEncoder:
    // Длина соли: 16 байт
    // Длина хеша: 32 байта
    // Параллелизм: 1
    // Стоимость памяти: 1 << 14
    // Итерации: 2
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new Argon2PasswordEncoder(16, 32, 1, 1 << 14, 2);
    }

}
