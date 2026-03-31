package com.hackathon.backend.repository;

import com.hackathon.backend.model.Role;
import com.hackathon.backend.model.Role.RoleName;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    Optional<Role> findByRoleName(RoleName roleName);
}
