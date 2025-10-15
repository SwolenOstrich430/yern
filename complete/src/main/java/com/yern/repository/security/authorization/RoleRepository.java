package com.yern.repository.security.authorization;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yern.model.security.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

}
