package pt.ipma.recrutamento.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.ipma.recrutamento.domain.JobStage;

import java.util.List;
import java.util.Optional;

public interface JobStageRepository extends JpaRepository<JobStage, Long> {
    List<JobStage> findByJobIdOrderBySequenceAsc(Long jobId);
    Optional<JobStage> findByJobIdAndStageCode(Long jobId, pt.ipma.recrutamento.domain.enums.StageCode stageCode);
}
