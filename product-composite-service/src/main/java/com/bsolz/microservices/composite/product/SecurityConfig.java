package com.bsolz.microservices.composite.product;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.springframework.http.HttpMethod.POST;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity httpSecurity) {
        httpSecurity
                .authorizeExchange(authorizeExchangeSpec -> authorizeExchangeSpec
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers("/openapi/**").permitAll()
                        .pathMatchers("/webjars/**").permitAll()
                        .pathMatchers(POST, "/product-composite/**").hasAnyAuthority("SCOPE_product:write")
                        .anyExchange().authenticated()

                ).oauth2ResourceServer(oAuth2ResourceServerSpec -> oAuth2ResourceServerSpec.jwt(Customizer.withDefaults()));

        return httpSecurity.build();
    }
}
