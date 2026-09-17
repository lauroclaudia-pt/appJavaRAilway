package pt.ipma.recrutamento.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.ipma.recrutamento.domain.Vaga;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VagaRepository extends JpaRepository<Vaga, Long> {

    Optional<Vaga> findByVagaCode(String vagaCode);

    List<Vaga> findByWebsitePublishedTrueAndDeadlineDateAfter(LocalDateTime now);

    boolean existsByVagaCode(String vagaCode);
}
