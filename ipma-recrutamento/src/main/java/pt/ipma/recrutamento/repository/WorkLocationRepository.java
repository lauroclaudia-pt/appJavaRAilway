package pt.ipma.recrutamento.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.ipma.recrutamento.domain.WorkLocation;

public interface WorkLocationRepository extends JpaRepository<WorkLocation, Long> {
}
