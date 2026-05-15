package com.example.task_manager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    @Profile("dev")
    public SecurityFilterChain devSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/h2-console/**").permitAll()
                .anyRequest().permitAll()
            )
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions().sameOrigin());
        return http.build();

    }


    @Bean
    @Profile("prod")
    public SecurityFilterChain prodSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/users/register").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults())
            .formLogin(form -> form
            .defaultSuccessUrl("/tasks", true)  // ← добавляем редирект
        )
            .csrf(csrf -> csrf.disable());
        return http.build();
    }
    
}
