package br.com.petzon.petzonapi.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true) // Necessário para @PreAuthorize
public class SecurityConfiguration {

    private final TokenService tokenService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.headers().frameOptions().disable().and()
                .cors().and()
                .csrf().disable()
                .authorizeHttpRequests((authz) -> authz
                        // --- Rotas Públicas ---
                        .antMatchers("/api/auth/login", "/api/auth/register").permitAll()
                        .antMatchers(HttpMethod.GET, "/api/pets/**").permitAll()
                        .antMatchers("/ws/**").permitAll()
                        .antMatchers("/actuator/**").permitAll()

                        // --- Rotas de ONG ---
                        .antMatchers(HttpMethod.POST, "/api/pets").hasRole("ONG")
                        .antMatchers(HttpMethod.PUT, "/api/pets/**").hasRole("ONG")
                        .antMatchers(HttpMethod.DELETE, "/api/pets/**").hasRole("ONG")
                        .antMatchers("/api/ong/**").hasRole("ONG") // <-- NOVA REGRA PARA O ONG CONTROLLER

                        // --- Rotas de ADMIN ---
                        .antMatchers("/api/admin/**").hasRole("ADMIN")

                        // --- Rotas de Usuário Logado ---
                        .antMatchers(HttpMethod.GET, "/api/auth/usuario-logado").authenticated()
                        .antMatchers(HttpMethod.GET, "/api/chat/history/**").hasAnyRole("ONG", "USER")

                        // Qualquer outra requisição precisa de autenticação
                        .anyRequest().authenticated()
                );
        http.addFilterBefore(new TokenAuthenticationFilter(tokenService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().antMatchers(
                "/v3/api-docs",
                "/v3/api-docs/**",
                "/swagger-resources/**",
                "/swagger-ui/**");
    }

    @Bean
    WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedMethods("*")
                        .exposedHeaders("Authorization");
            }
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
