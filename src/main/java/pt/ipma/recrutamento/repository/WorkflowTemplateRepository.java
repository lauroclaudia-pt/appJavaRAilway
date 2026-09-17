package pt.ipma.recrutamento.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.ipma.recrutamento.domain.WorkflowTemplate;
import pt.ipma.recrutamento.domain.enums.OfferType;

import java.util.Optional;

public interface WorkflowTemplateRepository extends JpaRepository<WorkflowTemplate, Long> {
    Optional<WorkflowTemplate> findByOfferTypeAndActiveTrue(OfferType offerType);
}
