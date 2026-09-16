package pt.ipma.recrutamento.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.ipma.recrutamento.domain.Applicant;
import pt.ipma.recrutamento.domain.enums.ApplicantState;

import java.util.List;
import java.util.Optional;

public interface ApplicantRepository extends JpaRepository<Applicant, Long> {

    List<Applicant> findByJobId(Long jobId);

    List<Applicant> findByJobIdAndState(Long jobId, ApplicantState state);

    Optional<Applicant> findByJobIdAndIdNif(Long jobId, String idNif);

    boolean existsByJobIdAndIdNif(Long jobId, String idNif);

    long countByJobId(Long jobId);

    long countByJobIdAndStateIsNull(Long jobId);
}
