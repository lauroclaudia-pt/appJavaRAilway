package pt.ipma.recrutamento.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ipma.recrutamento.domain.SelectOption;
import pt.ipma.recrutamento.domain.enums.OptionCategory;
import pt.ipma.recrutamento.repository.SelectOptionRepository;
import pt.ipma.recrutamento.web.dto.SelectOptionRequest;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Gestão das opções configuráveis dos campos "select" da Vaga/Candidatura
 * (Vínculo, Regime, Nível Habilitacional, ...). Permite à Administração criar,
 * alterar e apagar opções, cada uma com uma janela de validade (dataInício/dataFim).
 */
@Service
@RequiredArgsConstructor
public class SelectOptionService {

    private final SelectOptionRepository repository;

    /** Todas as opções da categoria (incluindo futuras/expiradas) — para o ecrã de Administração. */
    public List<SelectOption> listAll(OptionCategory category) {
        return repository.findByCategoryOrderBySortOrderAscLabelAsc(category);
    }

    /** Apenas as opções atualmente válidas — para preencher os campos select dos formulários. */
    public List<SelectOption> listActive(OptionCategory category) {
        return repository.findByCategoryOrderBySortOrderAscLabelAsc(category).stream()
                .filter(SelectOption::isActive)
                .toList();
    }

    @Transactional
    public SelectOption create(SelectOptionRequest req) {
        validate(req, null);
        SelectOption option = new SelectOption();
        apply(option, req);
        return repository.save(option);
    }

    @Transactional
    public SelectOption update(Long id, SelectOptionRequest req) {
        validate(req, id);
        SelectOption option = getOrThrow(id);
        apply(option, req);
        return repository.save(option);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Opção não encontrada: " + id);
        }
        repository.deleteById(id);
    }

    private void apply(SelectOption option, SelectOptionRequest req) {
        option.setCategory(req.getCategory());
        option.setValue(req.getValue().trim());
        option.setLabel(req.getLabel().trim());
        option.setStartDate(req.getStartDate() != null ? req.getStartDate() : LocalDateTime.now());
        option.setEndDate(req.getEndDate());
        option.setSortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0);
    }

    private void validate(SelectOptionRequest req, Long editingId) {
        if (req.getEndDate() != null && req.getStartDate() != null && !req.getEndDate().isAfter(req.getStartDate())) {
            throw new IllegalArgumentException("A data de fim tem de ser posterior à data de início.");
        }
        boolean duplicate = (editingId == null)
                ? repository.existsByCategoryAndValueIgnoreCase(req.getCategory(), req.getValue().trim())
                : repository.existsByCategoryAndValueIgnoreCaseAndIdNot(req.getCategory(), req.getValue().trim(), editingId);
        if (duplicate) {
            throw new IllegalArgumentException("Já existe uma opção com este código nesta categoria.");
        }
    }

    private SelectOption getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Opção não encontrada: " + id));
    }
}
