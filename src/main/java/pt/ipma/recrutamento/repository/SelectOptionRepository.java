package pt.ipma.recrutamento.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.ipma.recrutamento.domain.SelectOption;
import pt.ipma.recrutamento.domain.enums.OptionCategory;

import java.util.List;

public interface SelectOptionRepository extends JpaRepository<SelectOption, Long> {

    List<SelectOption> findByCategoryOrderBySortOrderAscLabelAsc(OptionCategory category);

    boolean existsByCategoryAndValueIgnoreCase(OptionCategory category, String value);

    boolean existsByCategoryAndValueIgnoreCaseAndIdNot(OptionCategory category, String value, Long id);
}
