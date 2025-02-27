package com.mehmet.brokagefirm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class BrokageSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf((csrf) -> csrf.disable())
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/login")
                        .permitAll()
                        .requestMatchers("/orders/match/**").hasRole("ADMIN")
                        .requestMatchers("/orders/create/**", "/orders/list/**", "/orders/cancel/**", "/assets/**").hasAnyRole("ADMIN", "USER")
                        .anyRequest()
                        .authenticated()
                ).httpBasic(Customizer.withDefaults());

        return http.build();
    }
}