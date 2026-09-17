package pt.ipma.recrutamento.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.ipma.recrutamento.domain.Department;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
}
