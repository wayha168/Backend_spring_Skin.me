package com.project.skin_me.config;

import com.project.skin_me.security.jwt.AuthTokenFilter;
import com.project.skin_me.security.jwt.JwtAuthEntryPoint;
import com.project.skin_me.security.user.ShopUserDetailsService;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final ShopUserDetailsService userDetailsService;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;

    private static final String[] PUBLIC_API = {
            "/api/v1/users/**",
            "/api/v1/products/**",
            "/api/v1/categories/**",
            "/api/v1/images/**",
            "/api/v1/payment/webhook",
            "/api/v1/auth/**",
            "/api/v1/popular/**",
            "/api/v1/chat/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/webjars/**",
            "/login-page",
            "/signup",
            "/reset-password",
            "/css/**",
            "/js/**"
    };

    private static final String[] SECURED_API = {
            "/api/v1/carts/**",
            "/api/v1/favorites/**",
            "/api/v1/cartItems/**",
            "/api/v1/payment/**",
            "/api/v1/orders/**",
            "/api/v1/popular/user/**"
    };

    private static final String[] ADMIN_URLS = {
            "/dashboard/**",
            "/products/**",
            "/categories/**",
            "/sales/**",
            "/api/v1/admin/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(e -> e.authenticationEntryPoint(authenticationEntryPoint()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_API).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(ADMIN_URLS).hasRole("ADMIN")
                        .requestMatchers(SECURED_API).authenticated()
                        .requestMatchers("/").authenticated()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login-page")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login-page?logout")
                        .permitAll())
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**", "/v3/api-docs/**", "/swagger-ui/**"));

        http.addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            String path = request.getRequestURI();
            if (path.startsWith("/api/")) {
                // Use JwtAuthEntryPoint for API requests
                jwtAuthEntryPoint.commence(request, response, authException);
            } else {
                // Use LoginUrlAuthenticationEntryPoint for web requests
                new LoginUrlAuthenticationEntryPoint("/login-page").commence(request, response, authException);
            }
        };
    }

    @Bean
    public DaoAuthenticationProvider daoAuthProvider() {
        var provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthTokenFilter jwtFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:8800",
                "https://skinme.store",
                "https://www.skinme.store",
                "https://backend.skinme.store"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public OpenAPI apiDocs() {
        return new OpenAPI()
                .info(new Info()
                        .title("Skin Me API")
                        .version("1.0")
                        .description("API documentation for Skin Me project"))
                .addServersItem(new io.swagger.v3.oas.models.servers.Server()
                        .url("https://backend.skinme.store")
                        .description("Production server"))
                .addServersItem(new io.swagger.v3.oas.models.servers.Server()
                        .url("http://localhost:8800")
                        .description("Local server"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}