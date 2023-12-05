package com.example.webapplication.security;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.server.ErrorPageRegistrar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.nio.file.AccessDeniedException;

import static org.springframework.security.config.Customizer.withDefaults;


@EnableWebSecurity
@Configuration
public class SecurityConfiguration {


    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailServiceSecurity();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(encoder());
        return provider;
    }





    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder(10);
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authenticationProvider(authenticationProvider())
                  // Use this instead
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                // Require authentication for Assignment APIs
                                .requestMatchers(new MvcRequestMatcher(null, "/v2/assignments/**")).fullyAuthenticated()
                                .anyRequest().permitAll() // Allow other requests
                )
                .httpBasic(withDefaults())
                .sessionManagement(sessionManagement ->
                sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)


        );

        // Use Basic Authentication

        return http.build();
    }

//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
//
//        http.csrf()
//                .disable()
//                .authorizeRequests()
//                .antMatchers("/v1/account/*")
//                .fullyAuthenticated()
//                .anyRequest()
//                .permitAll()
//                .and()
//                .httpBasic();
//        return http.build();
//    }


}


