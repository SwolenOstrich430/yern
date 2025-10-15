package com.yern.repository.security.authorization;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yern.model.security.ResourceType;
import com.yern.model.security.authorization.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> getRoleById(Long roleId);
    List<Role> findByResource(ResourceType resource);
}
