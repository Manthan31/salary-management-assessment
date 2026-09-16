package com.acme.salary.reference;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRoleRepository extends JpaRepository<JobRole, Long> {
    Optional<JobRole> findByTitle(String title);
}
