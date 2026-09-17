package pt.ipma.recrutamento.config;

import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Sem isto, o Jackson tenta serializar diretamente a classe-proxy gerada pelo
 * Hibernate (ByteBuddyInterceptor) sempre que uma relação "lazy" ainda não foi
 * inicializada, resultando em "Type definition error: ... ByteBuddyInterceptor".
 * O Hibernate6Module ensina o Jackson a desembrulhar o proxy (ou a serializar
 * como null, se ainda não estiver inicializado, em vez de rebentar).
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Hibernate6Module hibernate6Module() {
        Hibernate6Module module = new Hibernate6Module();
        module.configure(Hibernate6Module.Feature.FORCE_LAZY_LOADING, true);
        return module;
    }
}
