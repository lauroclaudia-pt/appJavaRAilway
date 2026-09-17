package pt.ipma.recrutamento.web;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pt.ipma.recrutamento.domain.enums.Role;
import pt.ipma.recrutamento.repository.DepartmentRepository;
import pt.ipma.recrutamento.repository.TrabalhadorRepository;
import pt.ipma.recrutamento.repository.WorkLocationRepository;

import java.util.List;
import java.util.Map;

/**
 * Listas de apoio para preencher os formulários do BackOffice (ex.: escolher o
 * Gestor de RH, o Júri, a Unidade Orgânica ou o Local de Trabalho ao abrir uma
 * vaga). Devolve apenas os campos necessários — nunca a password.
 */
@RestController
@RequestMapping("/api/backoffice")
@RequiredArgsConstructor
public class BackOfficeReferenceController {

    private final TrabalhadorRepository trabalhadorRepository;
    private final DepartmentRepository departmentRepository;
    private final WorkLocationRepository workLocationRepository;

    /**
     * Lista de trabalhadores ativos, opcionalmente filtrada por uma responsabilidade
     * (?role=GESTOR_RH, ?role=CDRH, ?role=JURI, ...). Sem filtro, devolve todos.
     */
    @GetMapping("/users")
    public List<Map<String, Object>> listUsers(@RequestParam(required = false) Role role) {
        return trabalhadorRepository.findAll().stream()
                .filter(t -> t.isActive())
                .filter(t -> role == null || t.hasResponsabilidade(role))
                .map(t -> Map.<String, Object>of(
                        "id", t.getId(),
                        "name", t.getName(),
                        "responsabilidades", t.getResponsabilidades().stream().map(Enum::name).sorted().toList(),
                        "hasLogin", t.hasLogin()))
                .toList();
    }

    @GetMapping("/departments")
    public List<Map<String, Object>> listDepartments() {
        return departmentRepository.findAll().stream()
                .map(d -> Map.<String, Object>of("id", d.getId(), "name", d.getName()))
                .toList();
    }

    @GetMapping("/work-locations")
    public List<Map<String, Object>> listWorkLocations() {
        return workLocationRepository.findAll().stream()
                .filter(w -> w.isActive())
                .map(w -> Map.<String, Object>of(
                        "id", w.getId(), "displayName", w.getDisplayName(),
                        "district", w.getDistrict(), "municipality", w.getMunicipality()))
                .toList();
    }
}
