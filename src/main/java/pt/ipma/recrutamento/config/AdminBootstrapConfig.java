package pt.ipma.recrutamento.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import pt.ipma.recrutamento.domain.AppUser;
import pt.ipma.recrutamento.domain.enums.Role;
import pt.ipma.recrutamento.repository.AppUserRepository;

/**
 * Cria o utilizador administrador inicial a partir de variáveis de ambiente
 * (ADMIN_EMAIL / ADMIN_PASSWORD), caso ainda não exista nenhum. Necessário porque
 * não existe UI de registo para o primeiro acesso ao BackOffice.
 */
@Slf4j
@Configuration
public class AdminBootstrapConfig {

    @Bean
    public CommandLineRunner bootstrapAdmin(AppUserRepository repo,
                                             PasswordEncoder encoder,
                                             @Value("${ADMIN_EMAIL:admin@ipma.pt}") String adminEmail,
                                             @Value("${ADMIN_PASSWORD:}") String adminPassword) {
        return args -> {
            if (repo.count() > 0) return;

            String password = (adminPassword == null || adminPassword.isBlank())
                    ? "ChangeMe123!"
                    : adminPassword;

            AppUser admin = new AppUser();
            admin.setName("Administrador");
            admin.setEmail(adminEmail);
            admin.setPasswordHash(encoder.encode(password));
            admin.setRole(Role.ADMIN);
            admin.setActive(true);
            repo.save(admin);

            log.warn("Utilizador administrador criado: {} — defina ADMIN_PASSWORD nas variáveis de " +
                    "ambiente do Railway para não usar a password por omissão.", adminEmail);
        };
    }
}
