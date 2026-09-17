package com.acme.salary.reference;

import java.util.Comparator;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read-only reference data used to populate filter dropdowns and create/edit
 * forms in the UI (countries, departments, roles). Returned sorted by name for
 * stable presentation.
 */
@RestController
@RequestMapping("/api/meta")
public class ReferenceController {

    private final CountryRepository countryRepository;
    private final DepartmentRepository departmentRepository;
    private final JobRoleRepository jobRoleRepository;

    public ReferenceController(CountryRepository countryRepository,
                               DepartmentRepository departmentRepository,
                               JobRoleRepository jobRoleRepository) {
        this.countryRepository = countryRepository;
        this.departmentRepository = departmentRepository;
        this.jobRoleRepository = jobRoleRepository;
    }

    public record CountryDto(Long id, String name, String isoCode, String currencyCode) {
    }

    public record NamedDto(Long id, String name) {
    }

    @GetMapping("/countries")
    public List<CountryDto> countries() {
        return countryRepository.findAll().stream()
                .map(c -> new CountryDto(c.getId(), c.getName(), c.getIsoCode(), c.getCurrencyCode()))
                .sorted(Comparator.comparing(CountryDto::name))
                .toList();
    }

    @GetMapping("/departments")
    public List<NamedDto> departments() {
        return departmentRepository.findAll().stream()
                .map(d -> new NamedDto(d.getId(), d.getName()))
                .sorted(Comparator.comparing(NamedDto::name))
                .toList();
    }

    @GetMapping("/roles")
    public List<NamedDto> roles() {
        return jobRoleRepository.findAll().stream()
                .map(r -> new NamedDto(r.getId(), r.getTitle()))
                .sorted(Comparator.comparing(NamedDto::name))
                .toList();
    }
}
