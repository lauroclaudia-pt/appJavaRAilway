package pt.ipma.recrutamento.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import pt.ipma.recrutamento.repository.AppUserRepository;

/**
 * Segurança por camada (secção 13.1/13.2 da spec):
 *  - /api/public/**  -> acesso anónimo (Website público)
 *  - /api/portal/**  -> candidato autenticado (grupo PORTAL) — cada um só vê os seus dados,
 *                       reforçado a nível de serviço (ex.: AppealService, ApplicantService)
 *  - /api/backoffice/** -> GESTOR_RH, JURI, CDRH, ADMIN, com restrições adicionais aplicadas
 *                       nos serviços (isolamento por vaga/júri — equivalente aos ir.rule do Odoo)
 *
 * Nota: a plataforma usa autenticação HTTP Basic sobre HTTPS (terminado no Railway) para manter
 * o MVP simples; para produção recomenda-se evoluir para OAuth2/JWT com refresh tokens.
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final AppUserRepository appUserRepository;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return (String email) -> appUserRepository.findByEmailIgnoreCase(email)
                .filter(pt.ipma.recrutamento.domain.AppUser::isActive)
                .map(u -> (UserDetails) org.springframework.security.core.userdetails.User
                        .withUsername(u.getEmail())
                        .password(u.getPasswordHash())
                        .authorities("ROLE_" + u.getRole().name())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Utilizador não encontrado: " + email));
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // API stateless; CSRF não aplicável (sem cookies de sessão de browser)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/*.html", "/css/**", "/js/**", "/img/**", "/favicon.ico").permitAll()
                .requestMatchers("/api/public/**", "/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/api/auth/**").authenticated()
                .requestMatchers("/api/portal/**").hasAnyRole("PORTAL", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/backoffice/vagas/**").hasAnyRole("GESTOR_RH", "CDRH", "JURI", "ADMIN")
                .requestMatchers("/api/backoffice/vagas/**").hasAnyRole("GESTOR_RH", "CDRH", "ADMIN")
                .requestMatchers("/api/backoffice/**").hasAnyRole("GESTOR_RH", "JURI", "CDRH", "ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults());
        return http.build();
    }
}
