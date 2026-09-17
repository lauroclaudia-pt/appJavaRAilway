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

    /** Vagas onde o trabalhador atua como gestor (titular/suplente) ou como júri (qualquer papel). */
    @org.springframework.data.jpa.repository.Query("""
            SELECT v FROM Vaga v WHERE
                v.gestor.id = :trabalhadorId OR v.gestorSuplente.id = :trabalhadorId OR
                v.juriPresidente.id = :trabalhadorId OR v.juriVogalEfetivo1.id = :trabalhadorId OR
                v.juriVogalEfetivo2.id = :trabalhadorId OR v.juriVogalSuplente1.id = :trabalhadorId OR
                v.juriVogalSuplente2.id = :trabalhadorId
            """)
    List<Vaga> findMinhas(@org.springframework.data.repository.query.Param("trabalhadorId") Long trabalhadorId);
}
