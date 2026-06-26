package ro.puk3p.sentinel.account.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
class SecurityConfig(
    private val jwtAuthFilter: JwtAuthFilter,
    private val apiKeyAuthFilter: ApiKeyAuthFilter,
    @Value("\${app.cors.allowed-origins:http://localhost:4200}")
    private val allowedOrigins: String,
) {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { it.configurationSource(corsConfigurationSource()) }
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .authorizeHttpRequests { auth ->
                auth
                    // Open: health, auth, and the hypermedia index.
                    .requestMatchers("/actuator/**", "/error").permitAll()
                    .requestMatchers(
                        HttpMethod.POST,
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/mfa",
                        "/api/auth/google",
                    ).permitAll()
                    .requestMatchers(HttpMethod.GET, "/api").permitAll()
                    // Edge-agent endpoints — API key (ROLE_AGENT) or a logged-in user.
                    .requestMatchers(
                        HttpMethod.POST,
                        "/api/devices/register",
                        "/api/devices/*/heartbeat",
                        "/api/traffic/stats",
                        "/api/alerts",
                        "/api/dns/queries",
                        "/api/clients",
                    ).hasAnyRole("AGENT", "USER")
                    .requestMatchers(HttpMethod.GET, "/api/devices/*/ruleset").hasAnyRole("AGENT", "USER")
                    // CORS preflight.
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    // Everything else under the API requires a logged-in user.
                    .anyRequest().authenticated()
            }
            .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val origins = allowedOrigins.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val config = CorsConfiguration()
        config.allowedOrigins = origins
        config.allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        config.allowedHeaders = listOf("*")
        config.allowCredentials = true
        config.maxAge = 3600
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/api/**", config)
        return source
    }
}
