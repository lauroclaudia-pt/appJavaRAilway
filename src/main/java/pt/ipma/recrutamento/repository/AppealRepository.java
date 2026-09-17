package pt.ipma.recrutamento.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.ipma.recrutamento.domain.Appeal;

import java.util.List;

public interface AppealRepository extends JpaRepository<Appeal, Long> {
    List<Appeal> findByApplicantId(Long applicantId);
    List<Appeal> findByJobId(Long jobId);
}
