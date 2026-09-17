package pt.ipma.recrutamento.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ipma.recrutamento.domain.WorkLocation;
import pt.ipma.recrutamento.repository.WorkLocationRepository;
import pt.ipma.recrutamento.web.dto.WorkLocationRequest;

import java.time.LocalDateTime;
import java.util.List;

/** Administração de Locais de Trabalho — inserir, alterar e apagar. Acesso restrito a ADMIN. */
@RestController
@RequestMapping("/api/admin/work-locations")
@RequiredArgsConstructor
public class AdminWorkLocationController {

    private final WorkLocationRepository repository;

    @GetMapping
    public List<WorkLocation> list() {
        return repository.findAll();
    }

    @PostMapping
    public WorkLocation create(@Valid @RequestBody WorkLocationRequest req) {
        WorkLocation loc = new WorkLocation();
        apply(loc, req);
        return repository.save(loc);
    }

    @PutMapping("/{id}")
    public WorkLocation update(@PathVariable Long id, @Valid @RequestBody WorkLocationRequest req) {
        WorkLocation loc = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Local de trabalho não encontrado: " + id));
        apply(loc, req);
        return repository.save(loc);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Local de trabalho não encontrado: " + id);
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void apply(WorkLocation loc, WorkLocationRequest req) {
        loc.setDisplayName(req.getDisplayName());
        loc.setDistrict(req.getDistrict());
        loc.setMunicipality(req.getMunicipality());
        loc.setAddress(req.getAddress());
        loc.setStartDate(req.getStartDate() != null ? req.getStartDate() : LocalDateTime.now());
        loc.setEndDate(req.getEndDate());
    }
}
