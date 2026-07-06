package com.andrei.demo.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

import java.util.List;

@Configuration
@AllArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers

                        .contentTypeOptions(Customizer.withDefaults())

                        .frameOptions(frame ->
                                frame.deny())

                        .referrerPolicy(ref ->
                                ref.policy(
                                        ReferrerPolicyHeaderWriter
                                                .ReferrerPolicy.NO_REFERRER
                                ))

                        .contentSecurityPolicy(csp ->
                                csp.policyDirectives(
                                        "default-src 'self'"
                                ))
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/login", "/auth/verify-2fa", "/error", "/password-reset/request", "/password-reset/confirm").permitAll()
                        .requestMatchers(HttpMethod.POST, "/person").hasRole("TEAM_MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/person/**").hasRole("TEAM_MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/person/**").hasAnyRole("TEAM_MANAGER", "DEPARTMENT_HEAD", "MEMBER")
                        .requestMatchers(HttpMethod.PATCH, "/person/**").hasAnyRole("TEAM_MANAGER", "DEPARTMENT_HEAD", "MEMBER")
                        .requestMatchers(HttpMethod.GET, "/person/**").authenticated()

                        .requestMatchers(HttpMethod.POST, "/departament").hasRole("TEAM_MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/departament/**").hasRole("TEAM_MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/departament/**").hasAnyRole("TEAM_MANAGER", "DEPARTMENT_HEAD")
                        .requestMatchers(HttpMethod.PATCH, "/departament/**").hasAnyRole("TEAM_MANAGER", "DEPARTMENT_HEAD")
                        .requestMatchers(HttpMethod.GET, "/departament/**").authenticated()

                        .requestMatchers(HttpMethod.POST, "/task").hasAnyRole("TEAM_MANAGER", "DEPARTMENT_HEAD")
                        .requestMatchers(HttpMethod.DELETE, "/task/**").hasRole("TEAM_MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/task/**").hasAnyRole("TEAM_MANAGER", "DEPARTMENT_HEAD")
                        .requestMatchers(HttpMethod.PATCH, "/task/**").hasAnyRole("TEAM_MANAGER", "DEPARTMENT_HEAD", "MEMBER")
                        .requestMatchers(HttpMethod.GET, "/task/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/audit-log/**").hasRole("TEAM_MANAGER")
                        .requestMatchers(HttpMethod.POST, "/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/logout").permitAll()
                        .requestMatchers(
                                HttpMethod.POST,
                                "/openai/**"
                        ).authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}