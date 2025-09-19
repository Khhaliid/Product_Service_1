package se.product_service_1.config;

import io.jsonwebtoken.security.Keys;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import java.util.Base64;
import javax.crypto.SecretKey;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final String jwtSecret = System.getenv("JWT_SECRET"); // or inject via @Value("${JWT_SECRET}")

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> {}) // Lambda-friendly 6.1+ syntax
                );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
        SecretKey secretKey = Keys.hmacShaKeyFor(keyBytes);
        return NimbusJwtDecoder.withSecretKey(secretKey).build();
    }
}
