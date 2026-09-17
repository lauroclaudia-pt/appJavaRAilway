package pt.ipma.recrutamento.web;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.ipma.recrutamento.repository.AppUserRepository;
import pt.ipma.recrutamento.repository.DepartmentRepository;
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

    private final AppUserRepository appUserRepository;
    private final DepartmentRepository departmentRepository;
    private final WorkLocationRepository workLocationRepository;

    @GetMapping("/users")
    public List<Map<String, Object>> listUsers() {
        return appUserRepository.findAll().stream()
                .filter(u -> u.isActive())
                .map(u -> Map.<String, Object>of("id", u.getId(), "name", u.getName(), "role", u.getRole().name()))
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
                .map(w -> Map.<String, Object>of(
                        "id", w.getId(), "displayName", w.getDisplayName(),
                        "district", w.getDistrict(), "municipality", w.getMunicipality()))
                .toList();
    }
}
