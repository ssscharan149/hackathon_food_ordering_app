package com.hackathon.backend.support;

import com.hackathon.backend.model.Role;
import com.hackathon.backend.model.Role.RoleName;
import com.hackathon.backend.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RoleDataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public RoleDataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) {
        createRoleIfMissing(RoleName.ADMIN);
        createRoleIfMissing(RoleName.USER);
    }

    private void createRoleIfMissing(RoleName roleName) {
        roleRepository.findByRoleName(roleName)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setRoleName(roleName);
                    return roleRepository.save(role);
                });
    }
}
